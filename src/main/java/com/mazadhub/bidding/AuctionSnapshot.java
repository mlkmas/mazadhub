package com.mazadhub.bidding;

import com.mazadhub.domain.ItemStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

// Read-only picture of an auction, holding just what the bid rules need to judge a bid
public record AuctionSnapshot(ItemStatus status, BigDecimal currentPrice, Instant endTime)
{
    // Compact constructor, all three fields are required
    public AuctionSnapshot
    {
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(currentPrice, "currentPrice");
        Objects.requireNonNull(endTime, "endTime");
    }

    // True while the auction is ACTIVE and its end time has not passed
    public boolean isOpenAt(Instant now)
    {
        return status==ItemStatus.ACTIVE&&now.isBefore(endTime);
    }
}
