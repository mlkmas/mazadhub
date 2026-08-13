package com.mazadhub.api.dto;

import java.math.BigDecimal;

/** Request body for POST /api/items/{id}/bids. The amount is the bidder's maximum. */
public record PlaceBidRequest(Long bidderId, BigDecimal amount) {
}
