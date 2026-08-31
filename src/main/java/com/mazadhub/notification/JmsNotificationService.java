package com.mazadhub.notification;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Topic;

import java.math.BigDecimal;
import java.util.logging.Logger;

// Publishes auction events to a JMS topic, so every browser watching the item gets a copy
// Enabled as an @Alternative in beans.xml; needs jms/mazadhubFactory and jms/auctionTopic in GlassFish
@Alternative
@ApplicationScoped
public class JmsNotificationService implements NotificationPort
{
    private static final Logger LOG=Logger.getLogger(JmsNotificationService.class.getName());

    // the JMS session used to send messages, injected by the container
    @Inject
    private JMSContext context;

    // the publish/subscribe topic every watcher listens to
    @Resource(lookup="jms/auctionTopic")
    private Topic auctionTopic;

    // Publishes a BID event with the new price
    @Override
    public void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId)
    {
        publish("BID", itemId, currentPrice, leaderId);
    }

    // Publishes a CLOSED event with the final price
    @Override
    public void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId)
    {
        publish("CLOSED", itemId, finalPrice, winnerId);
    }

    // Sends one event as small JSON, with the item id as a property so subscribers can filter
    private void publish(String type, long itemId, BigDecimal price, Long userId)
    {
        try
        {
            String json="{\"type\":\""+type
                    +"\",\"itemId\":"+itemId
                    +",\"price\":"+(price==null?"null":price.toPlainString())
                    +",\"userId\":"+(userId==null?"null":userId)
                    +"}";
            context.createProducer()
                    .setProperty("itemId", itemId)
                    .send(auctionTopic, json);
        }
        catch(RuntimeException e)
        {
            // Never let a messaging problem break a bid that already committed.
            LOG.warning("Could not publish "+type+" for item "+itemId+": "+e.getMessage());
        }
    }
}
