package com.mazadhub.api.dto;

import com.mazadhub.domain.Item;

import java.math.BigDecimal;
import java.time.Instant;

// Compact read model for catalogue / search results
public record ItemSummaryDTO(Long id, String title, String categoryName,
                             BigDecimal currentPrice, BigDecimal buyNowPrice,
                             Instant endDate, String status, long bidCount)
{
    // Copies the catalogue fields of an item into the DTO
    public static ItemSummaryDTO from(Item i, long bidCount)
    {
        return new ItemSummaryDTO(
                i.getId(),
                i.getTitle(),
                i.getCategory().getName(),
                i.getCurrentPrice(),
                i.getBuyNowPrice(),
                i.getEndDate(),
                i.getStatus().name(),
                bidCount);
    }
}
