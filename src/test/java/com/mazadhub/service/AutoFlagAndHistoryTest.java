package com.mazadhub.service;

import com.mazadhub.domain.Bid;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins down when a recorded row counts as "automatic", which is easy to misread on the history table
class AutoFlagAndHistoryTest
{
    private static final Instant NOW=Instant.parse("2026-06-11T12:00:00Z");

    private Fakes.Users users;
    private Fakes.Items items;
    private Fakes.Bids bids;
    private Fakes.AutoBids autoBids;
    private BiddingService service;

    private User seller, alice, bob;
    private Category category;

    // Fresh fakes and a service with a frozen clock before every test
    @BeforeEach
    void setUp()
    {
        users=new Fakes.Users();
        items=new Fakes.Items();
        bids=new Fakes.Bids();
        autoBids=new Fakes.AutoBids();
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

    // The most recently saved bid row
    private Bid last()
    {
        List<Bid> saved=bids.saved;
        return saved.get(saved.size()-1);
    }

    // Taking the lead yourself is a manual row
    @Test
    void firstBidderIsRecordedAsManual()
    {
        Item it=item("100");
        service.placeBid(it.getId(), alice.getId(), new BigDecimal("500"));

        assertFalse(last().isAuto(),
                "the actor became the leader themselves, so the row is manual");
        assertEquals(alice.getId(), last().getBidder().getId());
    }

    // A proxy defending its lead is an automatic row belonging to the defender, not the challenger
    @Test
    void proxyDefenceIsRecordedAsAutomaticAndBelongsToTheDefender()
    {
        Item it=item("100");
        service.placeAutoBid(it.getId(), alice.getId(), new BigDecimal("5000")); // hidden max
        service.placeBid(it.getId(), bob.getId(), new BigDecimal("500")); // bob challenges

        Bid row=last();
        assertTrue(row.isAuto(),
                "alice's proxy defended, so this row is automatic");
        assertEquals(alice.getId(), row.getBidder().getId(),
                "the row belongs to the defender (alice), not the challenger (bob)");
    }

    // Beating a proxy ceiling yourself is a manual row
    @Test
    void takingTheLeadFromSomeoneElseIsRecordedAsManual()
    {
        Item it=item("100");
        service.placeAutoBid(it.getId(), alice.getId(), new BigDecimal("500"));
        service.placeBid(it.getId(), bob.getId(), new BigDecimal("5000")); // bob beats her cap

        Bid row=last();
        assertFalse(row.isAuto(), "bob out-bid alice himself, so the row is manual");
        assertEquals(bob.getId(), row.getBidder().getId());
    }

    // Each action adds exactly one row, the resulting top offer, so losing bids are not stored
    @Test
    void aLosingBidDoesNotCreateItsOwnHistoryRow()
    {
        Item it=item("100");
        service.placeAutoBid(it.getId(), alice.getId(), new BigDecimal("5000"));
        int before=bids.saved.size();

        service.placeBid(it.getId(), bob.getId(), new BigDecimal("500")); // bob loses

        assertEquals(before+1, bids.saved.size(),
                "exactly one row is added per action (the resulting top offer)");
        assertEquals(alice.getId(), last().getBidder().getId(),
                "and it is attributed to the winner, not to bob");
    }

    // The newest row always equals the current price of the item
    @Test
    void everyRecordedRowMatchesThePriceAtThatMoment()
    {
        Item it=item("100");
        service.placeAutoBid(it.getId(), alice.getId(), new BigDecimal("5000"));
        service.placeBid(it.getId(), bob.getId(), new BigDecimal("500"));
        service.placeBid(it.getId(), bob.getId(), new BigDecimal("2000"));

        assertEquals(0, last().getAmount().compareTo(it.getCurrentPrice()),
                "the newest row must equal the item's current price");
    }
}
