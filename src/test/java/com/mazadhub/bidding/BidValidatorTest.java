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

// Unit tests for the bid validation rules, using a fixed clock
class BidValidatorTest
{
    private final BidValidator validator=new BidValidator(PriceIncrementRules.defaultRules());
    private final Instant now=Instant.parse("2026-06-11T12:00:00Z");
    private final Instant future=Instant.parse("2026-06-12T12:00:00Z");
    private final Instant past=Instant.parse("2026-06-10T12:00:00Z");

    // Builds an ACTIVE snapshot at the given price and end time
    private AuctionSnapshot active(String price, Instant end)
    {
        return new AuctionSnapshot(ItemStatus.ACTIVE, new BigDecimal(price), end);
    }

    // A bid exactly on the minimum is legal: 100 + 5 = 105
    @Test
    void acceptsBidAtExactlyMinimumNextBid()
    {
        // current 100 -> min increment 5 -> min next bid 105
        assertDoesNotThrow(()->validator.validate(active("100", future), new BigDecimal("105"), now));
    }

    // Anything above the minimum is legal too
    @Test
    void acceptsBidAboveMinimum()
    {
        assertDoesNotThrow(()->validator.validate(active("100", future), new BigDecimal("200"), now));
    }

    // One below the minimum is refused, and the exception reports the required 105
    @Test
    void rejectsBidBelowMinimumNextBid()
    {
        BidTooLowException ex=assertThrows(BidTooLowException.class,
                ()->validator.validate(active("100", future), new BigDecimal("104"), now));
        assertTrue(ex.getMinRequired().compareTo(new BigDecimal("105"))==0);
    }

    // Matching the current price is not enough, a step must be added
    @Test
    void rejectsBidEqualToCurrentPrice()
    {
        assertThrows(BidTooLowException.class,
                ()->validator.validate(active("100", future), new BigDecimal("100"), now));
    }

    // Any amount is refused once the end time has passed
    @Test
    void rejectsBidOnEndedAuction()
    {
        assertThrows(AuctionClosedException.class,
                ()->validator.validate(active("100", past), new BigDecimal("1000"), now));
    }

    // A SOLD auction takes no more bids
    @Test
    void rejectsBidOnNonActiveAuction()
    {
        AuctionSnapshot sold=new AuctionSnapshot(ItemStatus.SOLD, new BigDecimal("100"), future);
        assertThrows(AuctionClosedException.class,
                ()->validator.validate(sold, new BigDecimal("1000"), now));
    }

    // The same three cases through the boolean version
    @Test
    void isValid_returnsBooleanInsteadOfThrowing()
    {
        assertTrue(validator.isValid(active("100", future), new BigDecimal("105"), now));
        assertFalse(validator.isValid(active("100", future), new BigDecimal("101"), now));
        assertFalse(validator.isValid(active("100", past), new BigDecimal("1000"), now));
    }
}
