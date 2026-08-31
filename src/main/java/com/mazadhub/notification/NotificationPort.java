package com.mazadhub.notification;

import java.math.BigDecimal;

// How the services announce auction events, without knowing whether JMS is behind it
public interface NotificationPort
{
    // A new bid changed the current price / leader of an item
    void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId);

    // An auction closed (sold via buy-now, expiry, or final resolution)
    void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId);
}
