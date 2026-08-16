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

/**
 * Closes auctions whose end date has passed: marks each item SOLD (if it drew
 * at least one bid) or CLOSED (if it did not), deactivates any standing proxy
 * bids, and publishes the close notification.
 *
 * <p>Separated from {@link AuctionScheduler} so the timer only handles
 * <em>when</em> to run, while the closing rules live here and stay testable.
 * Each item is closed in its own transaction, so one failure cannot roll back
 * the others.
 */
@ApplicationScoped
public class AuctionCloser {

    private static final Logger LOG = Logger.getLogger(AuctionCloser.class.getName());

    @Inject
    private ItemRepository items;

    @Inject
    private AutoBidRepository autoBids;

    @Inject
    private NotificationPort notifier;

    /** Overridable in tests. */
    protected Instant now() {
        return Instant.now();
    }

    /**
     * Closes every auction that has expired.
     *
     * @return how many auctions were closed
     */
    public int closeExpiredAuctions() {
        List<Item> expired = items.findExpiredActive(now());
        int closed = 0;
        for (Item item : expired) {
            try {
                closeOne(item.getId());
                closed++;
            } catch (RuntimeException e) {
                // Keep going: one bad item must not stop the rest.
                LOG.warning("Could not close auction " + item.getId() + ": " + e.getMessage());
            }
        }
        return closed;
    }

    /**
     * Closes a single auction in its own transaction. The item row is locked
     * for update so a bid arriving at the same moment cannot interleave.
     */
    @Transactional
    public void closeOne(long itemId) {
        Item item = items.findByIdForUpdate(itemId).orElse(null);
        if (item == null || item.getStatus() != ItemStatus.ACTIVE) {
            return;   // already closed by someone else (e.g. buy-now)
        }

        // The current leader, if any, becomes the winner.
        User winner = item.getWinner();
        if (winner != null) {
            item.setStatus(ItemStatus.SOLD);
        } else {
            item.setStatus(ItemStatus.CLOSED);   // expired with no bids
        }
        items.save(item);

        // Standing proxy bids are finished with.
        for (AutoBid ab : autoBids.findActiveByItem(item)) {
            ab.setActive(false);
            autoBids.save(ab);
        }

        notifier.auctionClosed(item.getId(), item.getCurrentPrice(),
                winner == null ? null : winner.getId());

        LOG.info(() -> "Auction " + itemId + " closed with status " + item.getStatus());
    }
}
