package com.mazadhub.bidding;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * the maximum amount they are willing to
 * pay, and when they committed it. A plain manual bid is modelled as a standing
 * bid whose maximum equals the bid amount, so manual and automatic (proxy) bids
 * are resolved by the same engine.
 */
public record StandingBid(long bidderId, BigDecimal maxAmount, Instant placedAt)
{
    public StandingBid
    {
        Objects.requireNonNull(maxAmount, "maxAmount");
        Objects.requireNonNull(placedAt, "placedAt");
        if (maxAmount.signum() <= 0)
        {
            throw new IllegalArgumentException("maxAmount must be positive: " + maxAmount);
        }
    }
}
