package com.mazadhub.api.dto;

import com.mazadhub.domain.Bid;

import java.math.BigDecimal;
import java.time.Instant;

// Read model for a single row of bid history
public record BidDTO(BigDecimal amount, Instant bidTime, boolean auto)
{
    // Copies one bid row into the DTO
    public static BidDTO from(Bid b)
    {
        return new BidDTO(b.getAmount(), b.getBidTime(), b.isAuto());
    }
}
