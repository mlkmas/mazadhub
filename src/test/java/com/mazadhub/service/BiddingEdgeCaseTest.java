package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.exception.BuyNowNotAvailableException;
import com.mazadhub.exception.ItemNotFoundException;
import com.mazadhub.exception.UserNotFoundException;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Boundary and failure cases: amounts around the minimum, closed and expired auctions, buy-now and unknown ids
class BiddingEdgeCaseTest
{
    private static final Instant NOW=Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private BiddingService service;
    private User seller, alice, bob;
    private Category category;

    // Fresh fakes and a service with a frozen clock before every test
    @BeforeEach
    void setUp()
    {
        users=new Fakes.Users();
        items=new Fakes.Items();
        Fakes.Bids bids=new Fakes.Bids();
        Fakes.AutoBids autoBids=new Fakes.AutoBids();
        Fakes.Notifier notifier=new Fakes.Notifier();

        service=new BiddingService(items, bids, autoBids, users, notifier)
        {
            @Override
            protected Instant now()
            {
                return NOW;
            }
        };

        seller=users.save(new User("seller", "h", UserRole.USER));
        alice=users.save(new User("alice", "h", UserRole.USER));
        bob=users.save(new User("bob", "h", UserRole.USER));
        category=TestIds.withId(new Category("Test", "d"), 1L);
    }

    // An active auction of the test seller, ending in a day
    private Item item(String start)
    {
        return items.save(new Item(seller, category, "Item",
                new BigDecimal(start), NOW.plusSeconds(86400)));
    }

    // ---- amounts around the minimum -------------------------------------

    // Seven amounts under the 105 minimum are all refused
    @ParameterizedTest(name="bid {0} on a 100 auction is too low")
    @ValueSource(strings={"0", "1", "50", "99", "100", "104", "104.99"})
    void amountsBelowTheMinimumAreRejected(String amount)
    {
        Item it=item("100"); // min next bid = 105
        assertThrows(BidTooLowException.class,
                ()->service.placeBid(it.getId(), alice.getId(), new BigDecimal(amount)));
    }

    // The minimum itself and anything above it are accepted
    @ParameterizedTest(name="bid {0} on a 100 auction is accepted")
    @ValueSource(strings={"105", "105.01", "200", "10000"})
    void amountsAtOrAboveTheMinimumAreAccepted(String amount)
    {
        Item it=item("100");
        BidOutcome out=service.placeBid(it.getId(), alice.getId(), new BigDecimal(amount));
        assertEquals(alice.getId(), out.leaderId());
    }

    // On a 340 auction the exact minimum of 350 is accepted
    @Test
    void exactlyTheMinimumIsAccepted()
    {
        Item it=item("340"); // ladder: 340 -> +10 -> 350
        BidOutcome out=service.placeBid(it.getId(), alice.getId(), new BigDecimal("350"));
        assertEquals(alice.getId(), out.leaderId());
    }

    // ---- auction state ---------------------------------------------------

    // A SOLD item takes no more bids
    @Test
    void biddingOnASoldAuctionIsRejected()
    {
        Item it=item("100");
        it.setStatus(ItemStatus.SOLD);
        items.save(it);
        assertThrows(AuctionClosedException.class,
                ()->service.placeBid(it.getId(), alice.getId(), new BigDecimal("500")));
    }

    // A CLOSED item takes no more bids
    @Test
    void biddingOnAClosedAuctionIsRejected()
    {
        Item it=item("100");
        it.setStatus(ItemStatus.CLOSED);
        items.save(it);
        assertThrows(AuctionClosedException.class,
                ()->service.placeBid(it.getId(), alice.getId(), new BigDecimal("500")));
    }

    // Being still ACTIVE is not enough once the end time has passed
    @Test
    void biddingAfterTheEndDateIsRejected()
    {
        Item expired=items.save(new Item(seller, category, "Old",
                new BigDecimal("100"), NOW.minusSeconds(60)));
        assertThrows(AuctionClosedException.class,
                ()->service.placeBid(expired.getId(), alice.getId(), new BigDecimal("500")));
    }

    // ---- buy now ---------------------------------------------------------

    // Buy-now needs a buy-now price on the item
    @Test
    void buyNowWithoutAPriceIsRejected()
    {
        Item it=item("100"); // no buy-now price set
        assertThrows(BuyNowNotAvailableException.class,
                ()->service.buyNow(it.getId(), alice.getId()));
    }

    // Buy-now marks the item SOLD, at the buy-now price
    @Test
    void buyNowClosesTheAuctionAndSetsTheWinner()
    {
        Item it=item("100");
        it.setBuyNowPrice(new BigDecimal("600"));
        items.save(it);

        BidOutcome out=service.buyNow(it.getId(), alice.getId());

        assertEquals(ItemStatus.SOLD, it.getStatus());
        assertEquals(alice.getId(), it.getWinner().getId());
        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("600")));
    }

    // The second buyer is refused, so an item can sell only once
    @Test
    void buyNowOnAnAlreadySoldItemIsRejected()
    {
        Item it=item("100");
        it.setBuyNowPrice(new BigDecimal("600"));
        items.save(it);
        service.buyNow(it.getId(), alice.getId());

        assertThrows(AuctionClosedException.class,
                ()->service.buyNow(it.getId(), bob.getId()));
    }

    // ---- unknown ids -----------------------------------------------------

    // An unknown item id is reported clearly
    @Test
    void biddingOnAnUnknownItemIsRejected()
    {
        assertThrows(ItemNotFoundException.class,
                ()->service.placeBid(9999L, alice.getId(), new BigDecimal("500")));
    }

    // An unknown bidder id is reported clearly
    @Test
    void biddingAsAnUnknownUserIsRejected()
    {
        Item it=item("100");
        assertThrows(UserNotFoundException.class,
                ()->service.placeBid(it.getId(), 9999L, new BigDecimal("500")));
    }
}
