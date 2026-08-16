package com.mazadhub.notification;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Topic;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * JMS implementation of {@link NotificationPort}. Publishes auction events to a
 * publish/subscribe <em>topic</em>, so every interested subscriber (all browsers
 * currently watching an item) receives a copy — that is exactly the fan-out the
 * "live price update" requirement needs.
 *
 * <p>Marked {@code @Alternative} and enabled in {@code beans.xml}, so it takes
 * the place of {@link LoggingNotificationService} without touching any service
 * class — the whole point of programming against a port.
 *
 * <p>Requires these GlassFish resources (see the setup guide):
 * connection factory {@code jms/mazadhubFactory} and topic {@code jms/auctionTopic}.
 */
@Alternative
@ApplicationScoped
public class JmsNotificationService implements NotificationPort {

    private static final Logger LOG = Logger.getLogger(JmsNotificationService.class.getName());

    @Inject
    private JMSContext context;

    @Resource(lookup = "jms/auctionTopic")
    private Topic auctionTopic;

    @Override
    public void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId) {
        publish("BID", itemId, currentPrice, leaderId);
    }

    @Override
    public void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId) {
        publish("CLOSED", itemId, finalPrice, winnerId);
    }

    /**
     * Sends one event as a JMS message. The body is small JSON; the item id also
     * goes on a property so subscribers can filter by item with a JMS selector.
     */
    private void publish(String type, long itemId, BigDecimal price, Long userId) {
        try {
            String json = "{\"type\":\"" + type
                    + "\",\"itemId\":" + itemId
                    + ",\"price\":" + (price == null ? "null" : price.toPlainString())
                    + ",\"userId\":" + (userId == null ? "null" : userId)
                    + "}";
            context.createProducer()
                    .setProperty("itemId", itemId)
                    .send(auctionTopic, json);
        } catch (RuntimeException e) {
            // Never let a messaging problem break a bid that already committed.
            LOG.warning("Could not publish " + type + " for item " + itemId + ": " + e.getMessage());
        }
    }
}
