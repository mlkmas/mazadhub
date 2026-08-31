package com.mazadhub.api.dto;

import com.mazadhub.service.BidOutcome;

import java.math.BigDecimal;

// Response after a bidding action
public record BidOutcomeDTO(BigDecimal currentPrice, boolean youAreLeading, String status)
{
    // Copies the service result into the DTO
    public static BidOutcomeDTO from(BidOutcome o)
    {
        return new BidOutcomeDTO(o.currentPrice(), o.actorLeading(), o.status().name());
    }
}
