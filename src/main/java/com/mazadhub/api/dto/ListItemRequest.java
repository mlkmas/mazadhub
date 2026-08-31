package com.mazadhub.api.dto;

import java.math.BigDecimal;

// Request body for POST /api/items (list a new item for sale)
public record ListItemRequest(Long sellerId, Long categoryId, String title, String description,
                              BigDecimal startPrice, BigDecimal buyNowPrice,
                              int durationDays, String imageUrl)
{
}
