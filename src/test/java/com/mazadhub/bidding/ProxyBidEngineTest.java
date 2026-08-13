package com.mazadhub.bidding;

import com.mazadhub.pricing.PriceIncrementRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyBidEngineTest {

    private final ProxyBidEngine engine = new ProxyBidEngine(PriceIncrementRules.defaultRules());
    private final Instant t0 = Instant.parse("2026-06-11T10:00:00Z");

    private StandingBid bid(long bidder, String max, int secondsAfterT0) {
        return new StandingBid(bidder, new BigDecimal(max), t0.plusSeconds(secondsAfterT0));
    }

    private void assertPrice(BidResolution r, String expected) {
        assertEquals(0, r.currentPrice().compareTo(new BigDecimal(expected)),
                "expected price " + expected + " but was " + r.currentPrice());
    }

    @Test
    void noBids_priceIsStartingPrice_noWinner() {
        BidResolution r = engine.resolve(new BigDecimal("50"), List.of());
        assertTrue(r.winner().isEmpty());
        assertPrice(r, "50");
    }

    @Test
    void singleBidder_winsAtStartingPrice() {
        BidResolution r = engine.resolve(new BigDecimal("50"), List.of(bid(1, "300", 0)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "50");
    }

    @Test
    void twoBidders_winnerPaysRunnerUpPlusOneIncrement() {
        // A max 300, B max 200. Runner-up 200 -> increment at 200 is 10 -> price 210, winner A.
        BidResolution r = engine.resolve(new BigDecimal("50"),
                List.of(bid(1, "300", 0), bid(2, "200", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "210");
    }

    @Test
    void priceIsCappedAtWinnersMaximum() {
        // A max 205, B max 200. 200 + 10 = 210, but capped at A's max 205.
        BidResolution r = engine.resolve(new BigDecimal("50"),
                List.of(bid(1, "205", 0), bid(2, "200", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "205");
    }

    @Test
    void tieOnMaximum_earliestBidderWins_priceIsThatMaximum() {
        // Both max 200; bidder 1 committed first.
        BidResolution r = engine.resolve(new BigDecimal("50"),
                List.of(bid(2, "200", 5), bid(1, "200", 0)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "200");
    }

    @Test
    void priceNeverBelowStartingPrice() {
        // Two tiny maxes below the starting price floor.
        BidResolution r = engine.resolve(new BigDecimal("500"),
                List.of(bid(1, "60", 0), bid(2, "55", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "500");
    }

    @Test
    void manyBidders_onlyTopTwoDetermineOutcome() {
        BidResolution r = engine.resolve(new BigDecimal("100"),
                List.of(bid(1, "1000", 0), bid(2, "900", 1), bid(3, "400", 2), bid(4, "150", 3)));
        // Runner-up 900 -> increment at 900 is 10 -> 910, winner 1.
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "910");
    }

    @Test
    void incrementUsesRunnerUpTier() {
        // Runner-up max 1500 falls in the 50-increment tier -> 1550, winner pays that.
        BidResolution r = engine.resolve(new BigDecimal("100"),
                List.of(bid(1, "2000", 0), bid(2, "1500", 1)));
        assertEquals(1L, r.winner().orElseThrow());
        assertPrice(r, "1550");
    }
}
