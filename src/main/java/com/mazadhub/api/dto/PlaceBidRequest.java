package com.mazadhub.api.dto;

import java.math.BigDecimal;

// Request body for POST /api/items/{id}/bids
public record PlaceBidRequest(Long bidderId, BigDecimal amount)
{
}
