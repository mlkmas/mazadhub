package com.mazadhub.api.dto;

import com.mazadhub.domain.Bid;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read model for a single row of bid history. The bidder's identity is NOT
 * exposed — bids are anonymous to other users while the auction is live.
 */
public record BidDTO(BigDecimal amount, Instant bidTime, boolean auto) {

    public static BidDTO from(Bid b) {
        return new BidDTO(b.getAmount(), b.getBidTime(), b.isAuto());
    }
}
