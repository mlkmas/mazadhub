package com.mazadhub.api.dto;

import com.mazadhub.service.BidOutcome;

import java.math.BigDecimal;

/**
 * Response after a bidding action. Tells the caller the new price, whether they
 * are now the leader, and the item's status — without revealing who the leader
 * is (that stays confidential).
 */
public record BidOutcomeDTO(BigDecimal currentPrice, boolean youAreLeading, String status) {

    public static BidOutcomeDTO from(BidOutcome o) {
        return new BidOutcomeDTO(o.currentPrice(), o.actorLeading(), o.status().name());
    }
}
