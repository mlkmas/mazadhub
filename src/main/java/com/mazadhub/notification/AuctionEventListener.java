package com.mazadhub.notification;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import java.util.logging.Logger;

// Message-driven bean subscribing to the auction topic
@MessageDriven(activationConfig={
        @ActivationConfigProperty(propertyName="destinationLookup",
                                  propertyValue="jms/auctionTopic"),
        @ActivationConfigProperty(propertyName="destinationType",
                                  propertyValue="jakarta.jms.Topic")
})
// Message-driven bean that subscribes to the auction topic and logs what arrives, proving the JMS round trip works
public class AuctionEventListener implements MessageListener
{
    private static final Logger LOG=Logger.getLogger(AuctionEventListener.class.getName());

    // Called by the container for every message on the topic
    @Override
    public void onMessage(Message message)
    {
        try
        {
            if(message instanceof TextMessage text)
            {
                LOG.info(()->"Auction event received: "+safeBody(text));
            }
        }
        catch(RuntimeException e)
        {
            LOG.warning("Could not handle auction event: "+e.getMessage());
        }
    }

    // Reads the message text without letting a JMS error escape
    private String safeBody(TextMessage text)
    {
        try
        {
            return text.getText();
        }
        catch(Exception e)
        {
            return "<unreadable>";
        }
    }
}
