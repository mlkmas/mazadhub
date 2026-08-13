package com.mazadhub.bidding;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * The outcome of resolving the standing bids on an item: who is currently
 * winning and what the visible current price is.
 */
public record BidResolution(Long winningBidderId, BigDecimal currentPrice)
{

    public BidResolution
    {
        Objects.requireNonNull(currentPrice, "currentPrice");
    }

    /** optional because if there is no bids atall there is no winner */
    public Optional<Long> winner() {
        return Optional.ofNullable(winningBidderId);
    }

    public OptionalLong winnerId()
    {
        return winningBidderId == null ? OptionalLong.empty() : OptionalLong.of(winningBidderId);
    }
}
