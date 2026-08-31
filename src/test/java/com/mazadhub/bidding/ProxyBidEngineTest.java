package com.mazadhub.bidding;

import com.mazadhub.pricing.PriceIncrementRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Unit tests for the proxy engine, using standing maximums only
class ProxyBidEngineTest
{
    private final ProxyBidEngine engine=new ProxyBidEngine(PriceIncrementRules.defaultRules());
    private final Instant t0=Instant.parse("2026-06-11T10:00:00Z");

    // Builds one standing maximum committed at a known moment
    private StandingBid bid(long bidder, String max, int secondsAfterT0)
    {
        return new StandingBid(bidder, new BigDecimal(max), t0.plusSeconds(secondsAfterT0));
    }

    // Compares prices by value, so 210 and 210.00 count as equal
    private void assertPrice(BidResolution r, String expected)
    {
        assertEquals(0, r.currentPrice().compareTo(new BigDecimal(expected)),
                "expected price "+expected+" but was "+r.currentPrice());
    }

    // With nobody bidding the price stays at the start price
    @Test
    void noBids_priceIsStartingPrice_noWinner()
    {
        BidResolution r=engine.resolve(new BigDecimal("50"), List.of());
        assertTrue(r.winner().isEmpty());
        assertPrice(r, "50");
    }

    // A lone bidder leads at the start price, not at their maximum
    @Test
    void singleBidder_winsAtStartingPrice()
    {
        BidResolution r=engine.resolve(new BigDecimal("50"), List.of(bid(1, "300", 0)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "50");
    }

    // Maximums 300 and 200 give a price of 210: the runner-up plus one step
    @Test
    void twoBidders_winnerPaysRunnerUpPlusOneIncrement()
    {
        // A max 300, B max 200. Runner-up 200 -> increment at 200 is 10 -> price 210, winner A.
        BidResolution r=engine.resolve(new BigDecimal("50"),
                List.of(bid(1, "300", 0), bid(2, "200", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "210");
    }

    // The step never pushes the price past the winner maximum of 205
    @Test
    void priceIsCappedAtWinnersMaximum()
    {
        // A max 205, B max 200. 200 + 10 = 210, but capped at A's max 205.
        BidResolution r=engine.resolve(new BigDecimal("50"),
                List.of(bid(1, "205", 0), bid(2, "200", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "205");
    }

    // On equal maximums the earlier commitment wins, at that shared amount
    @Test
    void tieOnMaximum_earliestBidderWins_priceIsThatMaximum()
    {
        // Both max 200; bidder 1 committed first.
        BidResolution r=engine.resolve(new BigDecimal("50"),
                List.of(bid(2, "200", 5), bid(1, "200", 0)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "200");
    }

    // Maximums under the start price still leave the price at 500
    @Test
    void priceNeverBelowStartingPrice()
    {
        // Two tiny maxes below the starting price floor.
        BidResolution r=engine.resolve(new BigDecimal("500"),
                List.of(bid(1, "60", 0), bid(2, "55", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "500");
    }

    // With four bidders only the top two set the price
    @Test
    void manyBidders_onlyTopTwoDetermineOutcome()
    {
        BidResolution r=engine.resolve(new BigDecimal("100"),
                List.of(bid(1, "1000", 0), bid(2, "900", 1), bid(3, "400", 2), bid(4, "150", 3)));
        // Runner-up 900 -> increment at 900 is 10 -> 910, winner 1.
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "910");
    }

    // The step comes from the runner-up tier: 1500 + 50 = 1550
    @Test
    void incrementUsesRunnerUpTier()
    {
        // Runner-up max 1500 falls in the 50-increment tier -> 1550, winner pays that.
        BidResolution r=engine.resolve(new BigDecimal("100"),
                List.of(bid(1, "2000", 0), bid(2, "1500", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "1550");
    }
}
