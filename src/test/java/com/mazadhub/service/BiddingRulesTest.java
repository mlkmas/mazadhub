package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.exception.AlreadyHighestBidderException;
import com.mazadhub.exception.SellerCannotBidException;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the anti-abuse bidding rules added after the base engine:
 * a seller may not bid on / buy their own item, and a user may not out-bid
 * themselves while already leading.
 */
class BiddingRulesTest {

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private Fakes.Bids bids;
    private Fakes.AutoBids autoBids;
    private Fakes.Notifier notifier;
    private BiddingService service;

    private User seller;
    private User b1;
    private User b2;
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
    void sellerCannotPlaceBidOnOwnItem() {
        Item item = activeItem("50");
        assertThrows(SellerCannotBidException.class,
                () -> service.placeBid(item.getId(), seller.getId(), new BigDecimal("100")));
    }

    @Test
    void sellerCannotSetAutoBidOnOwnItem() {
        Item item = activeItem("50");
        assertThrows(SellerCannotBidException.class,
                () -> service.placeAutoBid(item.getId(), seller.getId(), new BigDecimal("100")));
    }

    @Test
    void sellerCannotBuyOwnItem() {
        Item item = activeItem("50");
        item.setBuyNowPrice(new BigDecimal("600"));
        assertThrows(SellerCannotBidException.class,
                () -> service.buyNow(item.getId(), seller.getId()));
    }

    @Test
    void cannotOutbidYourselfWhileLeading() {
        Item item = activeItem("50");
        // b1 bids and becomes the leader.
        service.placeBid(item.getId(), b1.getId(), new BigDecimal("100"));
        assertEquals(b1.getId(), item.getWinner().getId());

        // b1 bidding again (still the leader) is rejected.
        assertThrows(AlreadyHighestBidderException.class,
                () -> service.placeBid(item.getId(), b1.getId(), new BigDecimal("200")));
    }

    @Test
    void nonLeaderCanBidAgainAfterBeingOutbid() {
        Item item = activeItem("50");
        service.placeBid(item.getId(), b1.getId(), new BigDecimal("100")); // b1 leads
        service.placeBid(item.getId(), b2.getId(), new BigDecimal("200")); // b2 leads now

        // b1 is no longer the leader, so b1 may bid again.
        BidOutcome out = service.placeBid(item.getId(), b1.getId(), new BigDecimal("500"));
        assertEquals(b1.getId(), out.leaderId());
    }
}
