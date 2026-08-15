package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.support.Fakes;
import com.mazadhub.support.TestIds;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Diagnostic: replays a realistic alternating bid sequence and prints the
 * resulting price after each step, so the proxy behaviour can be inspected.
 *
 * <p>Expected proxy rule: the leader pays only ONE INCREMENT above the
 * runner-up's maximum — never their own full maximum (unless the two maxima
 * are equal, or the cap is reached).
 */
class ProxyBehaviourDiagnosticTest {

    private static final Instant NOW = Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private Fakes.Bids bids;
    private Fakes.AutoBids autoBids;
    private Fakes.Notifier notifier;
    private BiddingService service;

    private User seller, alice, bob;
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
        alice = users.save(new User("alice", "h", UserRole.USER));
        bob = users.save(new User("bob", "h", UserRole.USER));
        category = TestIds.withId(new Category("Media & Books", "d"), 1L);
    }

    private Item activeItem(String startPrice) {
        return items.save(new Item(seller, category, "Harry Potter",
                new BigDecimal(startPrice), NOW.plusSeconds(86400)));
    }

    @Test
    void proxyShouldOnlyPayOneIncrementAboveRunnerUp() {
        Item item = activeItem("100");

        // 1) Alice sets a hidden maximum of 30,000 while alone.
        BidOutcome s1 = service.placeBid(item.getId(), alice.getId(), new BigDecimal("30000"));
        System.out.println("STEP 1  alice max 30000 (alone)      -> price " + s1.currentPrice()
                + " | leader=" + s1.leaderId());
        // Alone: nobody pushed her up, so she leads at the START PRICE.
        assertEquals(0, s1.currentPrice().compareTo(new BigDecimal("100")),
                "A lone bidder must lead at the start price, not at their maximum");

        // 2) Bob bids 20,000 manually. Alice's proxy should counter automatically.
        BidOutcome s2 = service.placeBid(item.getId(), bob.getId(), new BigDecimal("20000"));
        System.out.println("STEP 2  bob bids 20000               -> price " + s2.currentPrice()
                + " | leader=" + s2.leaderId());
        // Runner-up 20,000 + increment 100 = 20,100 (NOT 30,000).
        assertEquals(0, s2.currentPrice().compareTo(new BigDecimal("20100")),
                "Alice's proxy must pay only one increment above Bob, not her full maximum");
        assertEquals(alice.getId(), s2.leaderId(), "Alice should still lead");

        // 3) Bob raises to 25,000. Alice's proxy counters again.
        BidOutcome s3 = service.placeBid(item.getId(), bob.getId(), new BigDecimal("25000"));
        System.out.println("STEP 3  bob raises to 25000          -> price " + s3.currentPrice()
                + " | leader=" + s3.leaderId());
        assertEquals(0, s3.currentPrice().compareTo(new BigDecimal("25100")),
                "Proxy should counter to 25,100");
        assertEquals(alice.getId(), s3.leaderId(), "Alice still leads (her cap is higher)");

        // 4) Bob finally exceeds Alice's hidden maximum.
        BidOutcome s4 = service.placeBid(item.getId(), bob.getId(), new BigDecimal("35000"));
        System.out.println("STEP 4  bob bids 35000 (beats cap)   -> price " + s4.currentPrice()
                + " | leader=" + s4.leaderId());
        // Alice's cap 30,000 + increment 100 = 30,100; Bob leads.
        assertEquals(0, s4.currentPrice().compareTo(new BigDecimal("30100")),
                "Bob should win at one increment above Alice's exhausted maximum");
        assertEquals(bob.getId(), s4.leaderId(), "Bob should now lead");

        System.out.println("Recorded bid rows: " + bids.saved.size());
    }
}
