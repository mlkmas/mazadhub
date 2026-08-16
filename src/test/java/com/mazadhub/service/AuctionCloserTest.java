package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests the auction-closing rules used by the scheduler: an expired auction
 * with bids becomes SOLD with a winner, one without bids becomes CLOSED, and an
 * auction that is already finished is left alone.
 */
class AuctionCloserTest {

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private Fakes.Bids bids;
    private Fakes.AutoBids autoBids;
    private Fakes.Notifier notifier;

    private BiddingService bidding;
    private AuctionCloser closer;

    private User seller, buyer;
    private Category category;

    @BeforeEach
    void setUp() {
        users = new Fakes.Users();
        items = new Fakes.Items();
        bids = new Fakes.Bids();
        autoBids = new Fakes.AutoBids();
        notifier = new Fakes.Notifier();

        bidding = new BiddingService(items, bids, autoBids, users, notifier) {
            @Override
            protected Instant now() {
                return NOW;
            }
        };

        closer = new AuctionCloser() {
            @Override
            protected Instant now() {
                // one hour after NOW, so auctions ending at NOW have expired
                return NOW.plusSeconds(3600);
            }
        };
        inject(closer, items, autoBids, notifier);

        seller = users.save(new User("seller", "h", UserRole.USER));
        buyer = users.save(new User("buyer", "h", UserRole.USER));
        category = TestIds.withId(new Category("Electronics", "d"), 1L);
    }

    /** Sets the closer's injected fields (no CDI container in a unit test). */
    private void inject(AuctionCloser target, Object... values) {
        for (Object value : values) {
            for (java.lang.reflect.Field f : AuctionCloser.class.getDeclaredFields()) {
                if (f.getType().isAssignableFrom(value.getClass())) {
                    try {
                        f.setAccessible(true);
                        if (f.get(target) == null) {
                            f.set(target, value);
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }

    private Item itemEndingAt(Instant end) {
        return items.save(new Item(seller, category, "Camera", new BigDecimal("100"), end));
    }

    @Test
    void expiredAuctionWithBidsBecomesSoldWithWinner() {
        Item item = itemEndingAt(NOW.plusSeconds(60));
        bidding.placeBid(item.getId(), buyer.getId(), new BigDecimal("150"));

        closer.closeOne(item.getId());

        assertEquals(ItemStatus.SOLD, item.getStatus());
        assertNotNull(item.getWinner(), "a bid was placed, so there must be a winner");
        assertEquals(buyer.getId(), item.getWinner().getId());
        assertEquals(1, notifier.auctionClosedCalls,
                "a close notification should have been published");
    }

    @Test
    void expiredAuctionWithoutBidsBecomesClosed() {
        Item item = itemEndingAt(NOW.plusSeconds(60));

        closer.closeOne(item.getId());

        assertEquals(ItemStatus.CLOSED, item.getStatus());
        assertNull(item.getWinner(), "nobody bid, so there is no winner");
    }

    @Test
    void alreadyFinishedAuctionIsLeftAlone() {
        Item item = itemEndingAt(NOW.plusSeconds(60));
        item.setStatus(ItemStatus.SOLD);
        items.save(item);

        closer.closeOne(item.getId());

        assertEquals(ItemStatus.SOLD, item.getStatus(), "status must not change");
    }
}
