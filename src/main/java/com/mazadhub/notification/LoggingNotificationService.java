package com.mazadhub.notification;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Default {@link NotificationPort} that logs events. Lets the application run
 * end-to-end before the JMS implementation (Day 9) is added; the JMS version
 * will implement the same port.
 */
@ApplicationScoped
public class LoggingNotificationService implements NotificationPort {

    private static final Logger LOG = Logger.getLogger(LoggingNotificationService.class.getName());

    @Override
    public void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId) {
        LOG.info(() -> "Bid placed on item " + itemId + ": price=" + currentPrice
                + ", leader=" + leaderId);
    }

    @Override
    public void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId) {
        LOG.info(() -> "Auction closed for item " + itemId + ": finalPrice=" + finalPrice
                + ", winner=" + winnerId);
    }
}
