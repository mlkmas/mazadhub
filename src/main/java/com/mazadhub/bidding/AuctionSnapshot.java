package com.mazadhub.bidding;

import com.mazadhub.domain.ItemStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * An immutable snapshot of the state of an auction at a point in time, holding
 * exactly what the bidding rules need to validate a bid.
 */
public record AuctionSnapshot(ItemStatus status, BigDecimal currentPrice, Instant endTime)
{

    public AuctionSnapshot
    {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(currentPrice, "currentPrice");
        Objects.requireNonNull(endTime, "endTime");
    }

    /** True if the auction is open for bidding at the given instant. */
    public boolean isOpenAt(Instant now)
    {
        return status == ItemStatus.ACTIVE && now.isBefore(endTime);
    }
}
