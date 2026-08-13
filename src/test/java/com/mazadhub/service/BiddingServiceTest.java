package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.exception.BuyNowNotAvailableException;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BiddingServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private Fakes.Bids bids;
    private Fakes.AutoBids autoBids;
    private Fakes.Notifier notifier;
    private BiddingService service;

    private User seller, b1, b2;
    private Category category;

    @BeforeEach
    void setUp() {
        users = new Fakes.Users();
        items = new Fakes.Items();
        bids = new Fakes.Bids();
        autoBids = new Fakes.AutoBids();
        notifier = new Fakes.Notifier();

        service = new BiddingService(items, bids, autoBids, users, notifier) {
            @Override
            protected Instant now() {
                return NOW;
            }
        };

        seller = users.save(new User("seller", "h", UserRole.USER));
        b1 = users.save(new User("buyer1", "h", UserRole.USER));
        b2 = users.save(new User("buyer2", "h", UserRole.USER));
        category = TestIds.withId(new Category("Electronics", "d"), 1L);
    }

    private Item activeItem(String startPrice) {
        Item item = new Item(seller, category, "Camera",
                new BigDecimal(startPrice), NOW.plusSeconds(3600));
        return items.save(item);
    }

    @Test
    void firstBidder_leadsAtStartPrice() {
        Item item = activeItem("50");

        BidOutcome out = service.placeBid(item.getId(), b1.getId(), new BigDecimal("100"));

        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("50")));
        assertEquals(b1.getId(), out.leaderId());
        assertTrue(out.actorLeading());
        assertEquals(b1.getId(), item.getWinner().getId());
        assertEquals(1, notifier.bidPlacedCalls);
    }

    @Test
    void higherProxyAutoCountersALowerBid() {
        Item item = activeItem("50");
        service.placeAutoBid(item.getId(), b1.getId(), new BigDecimal("300"));

        BidOutcome out = service.placeBid(item.getId(), b2.getId(), new BigDecimal("200"));

        // Runner-up 200 -> +10 increment -> 210, capped at b1's 300; b1 still leads.
        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("210")));
        assertEquals(b1.getId(), out.leaderId());
        assertFalse(out.actorLeading());
        // The last recorded bid is the system's auto counter on b1's behalf.
        assertTrue(bids.saved.get(bids.saved.size() - 1).isAuto());
    }

    @Test
    void higherMaximumWins() {
        Item item = activeItem("50");
        service.placeBid(item.getId(), b1.getId(), new BigDecimal("100"));

        BidOutcome out = service.placeBid(item.getId(), b2.getId(), new BigDecimal("200"));

        // Runner-up 100 -> +5 -> 105, b2 leads.
        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("105")));
        assertEquals(b2.getId(), out.leaderId());
        assertTrue(out.actorLeading());
    }

    @Test
    void bidBelowMinimumIsRejected() {
        Item item = activeItem("50");
        // current price 50 -> min next bid 55; 54 is too low.
        assertThrows(BidTooLowException.class,
                () -> service.placeBid(item.getId(), b1.getId(), new BigDecimal("54")));
    }

    @Test
    void bidOnEndedAuctionIsRejected() {
        Item ended = new Item(seller, category, "Old", new BigDecimal("50"), NOW.minusSeconds(10));
        items.save(ended);
        assertThrows(AuctionClosedException.class,
                () -> service.placeBid(ended.getId(), b1.getId(), new BigDecimal("1000")));
    }

    @Test
    void buyNowClosesTheAuction() {
        Item item = activeItem("50");
        item.setBuyNowPrice(new BigDecimal("600"));

        BidOutcome out = service.buyNow(item.getId(), b2.getId());

        assertEquals(ItemStatus.SOLD, out.status());
        assertEquals(b2.getId(), out.leaderId());
        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("600")));
        assertEquals(ItemStatus.SOLD, item.getStatus());
        assertEquals(1, notifier.auctionClosedCalls);

        // A second buy-now must fail: the auction is closed.
        assertThrows(AuctionClosedException.class,
                () -> service.buyNow(item.getId(), b1.getId()));
    }

    @Test
    void buyNowWithoutBuyNowPriceIsRejected() {
        Item item = activeItem("50"); // no buy-now price set
        assertThrows(BuyNowNotAvailableException.class,
                () -> service.buyNow(item.getId(), b2.getId()));
    }

    @Test
    void raisingOwnMaximumKeepsLead() {
        Item item = activeItem("50");
        service.placeBid(item.getId(), b1.getId(), new BigDecimal("100"));
        service.placeBid(item.getId(), b2.getId(), new BigDecimal("200")); // b2 leads at 105

        // b1 raises to 500 -> beats b2's 200 -> 210, b1 leads.
        BidOutcome out = service.placeBid(item.getId(), b1.getId(), new BigDecimal("500"));
        assertEquals(b1.getId(), out.leaderId());
        assertEquals(0, out.currentPrice().compareTo(new BigDecimal("210")));
    }
}
