package com.mazadhub.notification;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import java.util.logging.Logger;

/**
 * Message-driven bean subscribing to the auction topic. It demonstrates the
 * consuming half of the publish/subscribe flow and gives a server-side record of
 * every event; the browser-facing live updates are served by
 * {@code LivePriceResource}.
 */
@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup",
                                  propertyValue = "jms/auctionTopic"),
        @ActivationConfigProperty(propertyName = "destinationType",
                                  propertyValue = "jakarta.jms.Topic")
})
public class AuctionEventListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(AuctionEventListener.class.getName());

    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage text) {
                LOG.info(() -> "Auction event received: " + safeBody(text));
            }
        } catch (RuntimeException e) {
            LOG.warning("Could not handle auction event: " + e.getMessage());
        }
    }

    private String safeBody(TextMessage text) {
        try {
            return text.getText();
        } catch (Exception e) {
            return "<unreadable>";
        }
    }
}
