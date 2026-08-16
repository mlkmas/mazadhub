package com.mazadhub.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.util.logging.Logger;

/**
 * Timer service that sweeps for expired auctions once a minute and closes them.
 *
 * <p>{@code @Singleton @Startup} means the container creates exactly one
 * instance when the application deploys, and {@code @Schedule} registers a
 * container-managed timer — no threads to manage by hand. The actual closing
 * rules live in {@link AuctionCloser}.
 */
@Singleton
@Startup
public class AuctionScheduler {

    private static final Logger LOG = Logger.getLogger(AuctionScheduler.class.getName());

    @Inject
    private AuctionCloser closer;

    /**
     * Runs at second 0 of every minute. {@code persistent = false} keeps the
     * timer in memory instead of a database timer table, which is what you want
     * for a sweep that simply re-runs a minute later.
     */
    @Schedule(hour = "*", minute = "*", second = "0", persistent = false)
    public void sweep() {
        try {
            int closed = closer.closeExpiredAuctions();
            if (closed > 0) {
                LOG.info(() -> "AuctionScheduler closed " + closed + " expired auction(s)");
            }
        } catch (RuntimeException e) {
            LOG.warning("AuctionScheduler sweep failed: " + e.getMessage());
        }
    }
}
