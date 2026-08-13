package com.mazadhub.api.dto;

import com.mazadhub.domain.Item;
import com.mazadhub.pricing.PriceIncrementRules;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Full read model for the item-details screen. Includes the computed
 * {@code minNextBid} so a client can pre-fill / validate the bid field. The
 * seller's identity is still omitted.
 */
public record ItemDetailDTO(Long id, String title, String categoryName,
                            String description, String imageUrl,
                            BigDecimal startPrice, BigDecimal currentPrice, BigDecimal buyNowPrice,
                            BigDecimal minNextBid, Instant startDate, Instant endDate,
                            String status, long bidCount) {

    private static final PriceIncrementRules RULES = PriceIncrementRules.defaultRules();

    public static ItemDetailDTO from(Item i, long bidCount) {
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
