package com.mazadhub.notification;

import java.math.BigDecimal;

/**
 * Abstraction for broadcasting auction events. The service layer depends only
 * on this port, keeping it free of any messaging technology; the JMS-backed
 * implementation is wired in later. This also makes the services unit-testable
 * with a simple recording fake.
 */
public interface NotificationPort {

    /** A new bid changed the current price / leader of an item. */
    void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId);

    /** An auction closed (sold via buy-now, expiry, or final resolution). */
    void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId);
}
