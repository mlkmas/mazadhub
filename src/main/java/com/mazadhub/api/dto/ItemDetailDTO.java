package com.mazadhub.api.dto;

import com.mazadhub.domain.Item;
import com.mazadhub.pricing.PriceIncrementRules;

import java.math.BigDecimal;
import java.time.Instant;

// Full read model for the item-details screen
public record ItemDetailDTO(Long id, String title, String categoryName,
                            String description, String imageUrl,
                            BigDecimal startPrice, BigDecimal currentPrice, BigDecimal buyNowPrice,
                            BigDecimal minNextBid, Instant startDate, Instant endDate,
                            String status, long bidCount)
{
    // used to work out the minimum next bid shown on the page
    private static final PriceIncrementRules RULES=PriceIncrementRules.defaultRules();

    // Copies an item into the DTO and adds the minimum next bid
    public static ItemDetailDTO from(Item i, long bidCount)
    {
        return new ItemDetailDTO(
                i.getId(),
                i.getTitle(),
                i.getCategory().getName(),
                i.getDescription(),
                i.getImageUrl(),
                i.getStartPrice(),
                i.getCurrentPrice(),
                i.getBuyNowPrice(),
                RULES.minNextBid(i.getCurrentPrice()),
                i.getStartDate(),
                i.getEndDate(),
                i.getStatus().name(),
                bidCount);
    }
}
