package com.mazadhub.api.dto;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;

import java.math.BigDecimal;
import java.time.Instant;

// Result of a finished auction, shown to the SELLER only
public record SellerResultDTO(Long itemId, String title, String status,
                              BigDecimal finalPrice, boolean sold,
                              String winnerName, String winnerEmail, String winnerPhone,
                              Instant endDate)
{
    // Copies the result of a finished auction, filling the winner details only when it sold
    public static SellerResultDTO from(Item i)
    {
        boolean sold=i.getStatus()==ItemStatus.SOLD&&i.getWinner()!=null;
        User w=i.getWinner();
        return new SellerResultDTO(
                i.getId(),
                i.getTitle(),
                i.getStatus().name(),
                i.getCurrentPrice(),
                sold,
                sold?w.getFullName():null,
                sold?w.getEmail():null,
                sold?w.getPhone():null,
                i.getEndDate());
    }
}
