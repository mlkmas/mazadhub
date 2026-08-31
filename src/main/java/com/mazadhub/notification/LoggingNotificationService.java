package com.mazadhub.notification;

import jakarta.enterprise.context.ApplicationScoped;

import java.math.BigDecimal;
import java.util.logging.Logger;

// Default implementation: writes the events to the server log, used when JMS is not configured
@ApplicationScoped
public class LoggingNotificationService implements NotificationPort
{
    private static final Logger LOG=Logger.getLogger(LoggingNotificationService.class.getName());

    // Logs a new price
    @Override
    public void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId)
    {
        LOG.info(()->"Bid placed on item "+itemId+": price="+currentPrice
                +", leader="+leaderId);
    }

    // Logs a closed auction
    @Override
    public void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId)
    {
        LOG.info(()->"Auction closed for item "+itemId+": finalPrice="+finalPrice
                +", winner="+winnerId);
    }
}
