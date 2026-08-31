package com.mazadhub.api.dto;

// Request body for POST /api/items/{id}/buy-now
public record BuyNowRequest(Long bidderId)
{
}
