package com.mazadhub.bidding;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

// Who leads the auction right now and at what price
public record BidResolution(Long winningBidderId, BigDecimal currentPrice)
{
    // Compact constructor, a price is always required
    public BidResolution
    {
        Objects.requireNonNull(currentPrice, "currentPrice");
    }

    // The leader, empty while nobody has bid yet
    public Optional<Long> winner()
    {
        return Optional.ofNullable(winningBidderId);
    }

    // Same as winner(), as a primitive long for callers that want no boxing
    public OptionalLong winnerId()
    {
        return winningBidderId==null?OptionalLong.empty():OptionalLong.of(winningBidderId);
    }
}
