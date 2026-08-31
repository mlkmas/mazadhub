package com.mazadhub.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import java.util.logging.Logger;

// Container timer that wakes up every minute and asks AuctionCloser to close what has expired
@Singleton
@Startup
public class AuctionScheduler
{
    private static final Logger LOG=Logger.getLogger(AuctionScheduler.class.getName());

    // does the actual closing work
    @Inject
    private AuctionCloser closer;

    // Runs at second 0 of every minute; a failure is logged, never thrown at the timer
    @Schedule(hour="*", minute="*", second="0", persistent=false)
    public void sweep()
    {
        try
        {
            int closed=closer.closeExpiredAuctions();
            if(closed>0)
            {
                LOG.info(()->"AuctionScheduler closed "+closed+" expired auction(s)");
            }
        }
        catch(RuntimeException e)
        {
            LOG.warning("AuctionScheduler sweep failed: "+e.getMessage());
        }
    }
}
