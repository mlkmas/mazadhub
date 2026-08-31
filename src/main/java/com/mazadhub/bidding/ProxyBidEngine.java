package com.mazadhub.bidding;

import com.mazadhub.pricing.PriceIncrementRules;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// Works out the leader and the visible price from everyone's hidden maximums
// Classic proxy bidding: the highest maximum leads, but pays only one step above the runner-up
public final class ProxyBidEngine
{
    private final PriceIncrementRules rules;

    // Uses the given price-step table for the steps
    public ProxyBidEngine(PriceIncrementRules rules)
    {
        this.rules=Objects.requireNonNull(rules, "rules");
    }

    // Ranks the standing maximums and returns the leader plus the price they currently pay
    public BidResolution resolve(BigDecimal startingPrice, List<StandingBid> standingBids)
    {
        Objects.requireNonNull(startingPrice, "startingPrice");
        Objects.requireNonNull(standingBids, "standingBids");
        if(startingPrice.signum()<0)
        {
            throw new IllegalArgumentException("startingPrice must not be negative");
        }

        if(standingBids.isEmpty())
        {
            return new BidResolution(null, startingPrice);
        }

        // Highest max wins; earliest commitment breaks ties.
        List<StandingBid> ranked=standingBids.stream()
                .sorted(Comparator.comparing(StandingBid::maxAmount).reversed()
                        .thenComparing(StandingBid::placedAt))
                .toList();

        StandingBid leader=ranked.get(0);

        if(ranked.size()==1)
        {
            // Only one bidder: they win at the starting price (nobody pushed them up).
            return new BidResolution(leader.bidderId(), startingPrice);
        }

        StandingBid runnerUp=ranked.get(1);

        BigDecimal price;
        if(leader.maxAmount().compareTo(runnerUp.maxAmount())==0)
        {
            // Tie on the maximum: the price is that shared maximum; earliest wins.
            price=leader.maxAmount();
        }
        else
        {
            // Beat the runner-up by one increment, but never exceed the winner's max.
            BigDecimal increment=rules.minIncrement(runnerUp.maxAmount());
            price=runnerUp.maxAmount().add(increment).min(leader.maxAmount());
        }

        // Never below the starting price.
        price=price.max(startingPrice);

        return new BidResolution(leader.bidderId(), price);
    }
}
