package com.mazadhub.service;

import com.mazadhub.domain.AutoBid;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.notification.NotificationPort;
import com.mazadhub.repository.AutoBidRepository;
import com.mazadhub.repository.ItemRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;

// Closes auctions whose end time has passed: marks them SOLD or CLOSED, switches off proxy bids and announces the result
@ApplicationScoped
public class AuctionCloser
{
    private static final Logger LOG=Logger.getLogger(AuctionCloser.class.getName());

    // repositories and the notifier, injected by the container
    @Inject
    private ItemRepository items;

    @Inject
    private AutoBidRepository autoBids;

    @Inject
    private NotificationPort notifier;

    // Clock in one place, so tests can freeze time
    protected Instant now()
    {
        return Instant.now();
    }

    // Closes every expired auction and returns how many were closed
    public int closeExpiredAuctions()
    {
        List<Item> expired=items.findExpiredActive(now());
        int closed=0;
        for(Item item:expired)
        {
            try
            {
                closeOne(item.getId());
                closed++;
            }
            catch(RuntimeException e)
            {
                // Keep going: one bad item must not stop the rest.
                LOG.warning("Could not close auction "+item.getId()+": "+e.getMessage());
            }
        }

        return closed;
    }

    // Closes a single auction in its own transaction
    @Transactional
    public void closeOne(long itemId)
    {
        Item item=items.findByIdForUpdate(itemId).orElse(null);
        if(item==null||item.getStatus()!=ItemStatus.ACTIVE)
        {
            return; // already closed by someone else (e.g. buy-now)
        }

        // The current leader, if any, becomes the winner.
        User winner=item.getWinner();
        if(winner!=null)
        {
            item.setStatus(ItemStatus.SOLD);
        }
        else
        {
            item.setStatus(ItemStatus.CLOSED); // expired with no bids
        }

        items.save(item);

        // Standing proxy bids are finished with.
        for(AutoBid ab:autoBids.findActiveByItem(item))
        {
            ab.setActive(false);
            autoBids.save(ab);
        }

        notifier.auctionClosed(item.getId(), item.getCurrentPrice(),
                winner==null?null:winner.getId());

        LOG.info(()->"Auction "+itemId+" closed with status "+item.getStatus());
    }
}
