package com.mazadhub.bidding;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

// One bidder's standing commitment: the most they will pay, and when they committed it
public record StandingBid(long bidderId, BigDecimal maxAmount, Instant placedAt)
{
    // Compact constructor, the maximum must be positive
    public StandingBid
    {
        Objects.requireNonNull(maxAmount, "maxAmount");
        Objects.requireNonNull(placedAt, "placedAt");
        if(maxAmount.signum()<=0)
        {
            throw new IllegalArgumentException("maxAmount must be positive: "+maxAmount);
        }
    }
}
