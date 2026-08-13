package com.mazadhub.service;

import com.mazadhub.domain.ItemStatus;

import java.math.BigDecimal;

/**
 * The result of a bidding action, returned to the caller (UI / REST).
 *
 * @param currentPrice the item's price after the action
 * @param leaderId     the current leading bidder, or {@code null} if none
 * @param actorLeading whether the bidder who just acted is now the leader
 * @param status       the item's status after the action
 */
public record BidOutcome(BigDecimal currentPrice, Long leaderId,
                         boolean actorLeading, ItemStatus status) {
}
