package com.mazadhub.bidding;

import com.mazadhub.domain.ItemStatus;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.pricing.PriceIncrementRules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidValidatorTest {

    private final BidValidator validator = new BidValidator(PriceIncrementRules.defaultRules());
    private final Instant now = Instant.parse("2026-06-11T12:00:00Z");
    private final Instant future = Instant.parse("2026-06-12T12:00:00Z");
    private final Instant past = Instant.parse("2026-06-10T12:00:00Z");

    private AuctionSnapshot active(String price, Instant end) {
        return new AuctionSnapshot(ItemStatus.ACTIVE, new BigDecimal(price), end);
    }

    @Test
    void acceptsBidAtExactlyMinimumNextBid() {
        // current 100 -> min increment 5 -> min next bid 105
        assertDoesNotThrow(() -> validator.validate(active("100", future), new BigDecimal("105"), now));
    }

    @Test
    void acceptsBidAboveMinimum() {
        assertDoesNotThrow(() -> validator.validate(active("100", future), new BigDecimal("200"), now));
    }

    @Test
    void rejectsBidBelowMinimumNextBid() {
        BidTooLowException ex = assertThrows(BidTooLowException.class,
                () -> validator.validate(active("100", future), new BigDecimal("104"), now));
        assertTrue(ex.getMinRequired().compareTo(new BigDecimal("105")) == 0);
    }

    @Test
    void rejectsBidEqualToCurrentPrice() {
        assertThrows(BidTooLowException.class,
                () -> validator.validate(active("100", future), new BigDecimal("100"), now));
    }

    @Test
    void rejectsBidOnEndedAuction() {
        assertThrows(AuctionClosedException.class,
                () -> validator.validate(active("100", past), new BigDecimal("1000"), now));
    }

    @Test
    void rejectsBidOnNonActiveAuction() {
        AuctionSnapshot sold = new AuctionSnapshot(ItemStatus.SOLD, new BigDecimal("100"), future);
        assertThrows(AuctionClosedException.class,
                () -> validator.validate(sold, new BigDecimal("1000"), now));
    }

    @Test
    void isValid_returnsBooleanInsteadOfThrowing() {
        assertTrue(validator.isValid(active("100", future), new BigDecimal("105"), now));
        assertFalse(validator.isValid(active("100", future), new BigDecimal("101"), now));
        assertFalse(validator.isValid(active("100", past), new BigDecimal("1000"), now));
    }
}
