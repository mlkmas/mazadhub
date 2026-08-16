package com.mazadhub.service;

import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Randomised ("fuzz") testing. Each run drives hundreds of random bids at the
 * service and checks that the rules that must ALWAYS hold are never broken —
 * rather than checking one hand-picked example.
 *
 * <p>Every run uses a fixed seed, so a failure is reproducible: the seed is
 * printed in the failure message and the same sequence replays exactly.
 *
 * <p>Invariants checked after every single bid:
 * <ol>
 *   <li>the price never drops</li>
 *   <li>the price never falls below the starting price</li>
 *   <li>the price never exceeds the highest maximum anyone has committed</li>
 *   <li>there is always a leader once at least one bid succeeded</li>
 *   <li>the leader's own maximum is at least the current price</li>
 * </ol>
 */
class BiddingInvariantTest {

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");
    private static final int BIDS_PER_RUN = 300;

    @ParameterizedTest(name = "randomised auction, seed {0}")
    @ValueSource(longs = {1L, 2L, 7L, 42L, 99L, 123L, 2024L, 31337L})
    void invariantsHoldForRandomBidStreams(long seed) {
        Random random = new Random(seed);

        Fakes.Users users = new Fakes.Users();
        Fakes.Items items = new Fakes.Items();
        Fakes.Bids bids = new Fakes.Bids();
        Fakes.AutoBids autoBids = new Fakes.AutoBids();
        Fakes.Notifier notifier = new Fakes.Notifier();

        BiddingService service = new BiddingService(items, bids, autoBids, users, notifier) {
            @Override
            protected Instant now() {
                return NOW;
            }
        };

        User seller = users.save(new User("seller", "h", UserRole.USER));
        Category category = TestIds.withId(new Category("Test", "d"), 1L);

        BigDecimal startPrice = BigDecimal.valueOf(10 + random.nextInt(500));
        Item item = items.save(new Item(seller, category, "Item", startPrice,
                NOW.plusSeconds(86400)));

        // A pool of bidders (never the seller).
        List<User> bidders = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            bidders.add(users.save(new User("bidder" + i, "h", UserRole.USER)));
        }

        Map<Long, BigDecimal> committedMax = new HashMap<>();
        BigDecimal previousPrice = item.getCurrentPrice();
        int accepted = 0;

        for (int step = 0; step < BIDS_PER_RUN; step++) {
            User actor = bidders.get(random.nextInt(bidders.size()));
            // Random amount around the current price, sometimes far above, sometimes below.
            long base = item.getCurrentPrice().longValue();
            long amount = base + random.nextInt(2000) - 200;
            if (amount < 0) {
                amount = 0;
            }
            BigDecimal bid = BigDecimal.valueOf(amount);

            try {
                if (random.nextBoolean()) {
                    service.placeBid(item.getId(), actor.getId(), bid);
                } else {
                    service.placeAutoBid(item.getId(), actor.getId(), bid);
                }
                accepted++;
                committedMax.merge(actor.getId(), bid, (a, b) -> a.compareTo(b) >= 0 ? a : b);
            } catch (RuntimeException refused) {
                // Refusals are legitimate (too low, already leading, …) — skip.
                continue;
            }

            BigDecimal price = item.getCurrentPrice();
            String ctx = " [seed=" + seed + ", step=" + step + ", price=" + price + "]";

            // 1 + 2: monotonic, never below the start.
            assertTrue(price.compareTo(previousPrice) >= 0,
                    "price must never decrease" + ctx);
            assertTrue(price.compareTo(startPrice) >= 0,
                    "price must never fall below the starting price" + ctx);

            // 3: never above the biggest commitment anyone made.
            BigDecimal highestCommitment = committedMax.values().stream()
                    .max(BigDecimal::compareTo).orElse(startPrice);
            assertTrue(price.compareTo(highestCommitment) <= 0,
                    "price must never exceed the highest committed maximum" + ctx);

            // 4: someone must be leading.
            assertNotNull(item.getWinner(), "there must be a leader after a successful bid" + ctx);

            // 5: the leader can actually afford the current price.
            BigDecimal leaderMax = committedMax.get(item.getWinner().getId());
            assertNotNull(leaderMax, "the leader must have a committed maximum" + ctx);
            assertTrue(leaderMax.compareTo(price) >= 0,
                    "the leader's maximum must cover the current price" + ctx);

            previousPrice = price;
        }

        assertTrue(accepted > 0, "the random run should have accepted at least one bid");

        // Every recorded bid row must match the item's final state ordering.
        for (Bid recorded : bids.saved) {
            assertTrue(recorded.getAmount().compareTo(startPrice) >= 0,
                    "no recorded bid may sit below the starting price [seed=" + seed + "]");
        }
    }
}
