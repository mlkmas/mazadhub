package com.mazadhub.api.dto;

import java.math.BigDecimal;

// Request body for POST /api/items/{id}/autobid
public record AutoBidRequest(Long bidderId, BigDecimal maxAmount)
{
}
