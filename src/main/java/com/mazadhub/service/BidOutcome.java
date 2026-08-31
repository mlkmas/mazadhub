package com.mazadhub.service;

import com.mazadhub.domain.ItemStatus;

import java.math.BigDecimal;

// What a bidding action produced, handed back to the UI and the REST layer
public record BidOutcome(BigDecimal currentPrice, Long leaderId,
                         boolean actorLeading, ItemStatus status)
{
}
