package com.mazadhub.api.dto;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Result of a finished auction, shown to the SELLER only. This is the single
 * place the winner's contact details are revealed — and only once the item is
 * SOLD.
 */
public record SellerResultDTO(Long itemId, String title, String status,
                              BigDecimal finalPrice, boolean sold,
                              String winnerName, String winnerEmail, String winnerPhone,
                              Instant endDate) {

    public static SellerResultDTO from(Item i) {
        boolean sold = i.getStatus() == ItemStatus.SOLD && i.getWinner() != null;
        User w = i.getWinner();
        return new SellerResultDTO(
                i.getId(),
                i.getTitle(),
                i.getStatus().name(),
                i.getCurrentPrice(),
                sold,
                sold ? w.getFullName() : null,
                sold ? w.getEmail() : null,
                sold ? w.getPhone() : null,
                i.getEndDate());
    }
}
