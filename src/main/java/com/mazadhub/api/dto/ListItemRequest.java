package com.mazadhub.api.dto;

import java.math.BigDecimal;

/**
 * Request body for POST /api/items (list a new item for sale).
 * {@code buyNowPrice} may be null (no buy-now); {@code sellerId} identifies the
 * authenticated seller (authentication is added with the JSF session / a later
 * hardening pass, so for now the caller supplies it explicitly).
 */
public record ListItemRequest(Long sellerId, Long categoryId, String title, String description,
                              BigDecimal startPrice, BigDecimal buyNowPrice,
                              int durationDays, String imageUrl) {
}
