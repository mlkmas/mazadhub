package com.mazadhub.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// The price-step table: how much a new bid must add to the current price
// Default steps: up to 100 -> 5, up to 1000 -> 10, up to 10000 -> 50, above that -> 100
public final class PriceIncrementRules
{
    // One row of the table: prices up to this bound use this increment (null bound = the top, open-ended row)
    public record Tier(BigDecimal upperBoundInclusive, BigDecimal increment)
    {
        // Compact constructor, rejects a non-positive increment or bound
        public Tier
        {
            Objects.requireNonNull(increment, "increment");
            if(increment.signum()<=0)
            {
                throw new IllegalArgumentException("increment must be positive: "+increment);
            }

            if(upperBoundInclusive!=null&&upperBoundInclusive.signum()<=0)
            {
                throw new IllegalArgumentException("upper bound must be positive: "+upperBoundInclusive);
            }
        }
    }

    private final List<Tier> tiers;

    // Sorts the rows and checks there is exactly one open-ended top row
    public PriceIncrementRules(List<Tier> tiers)
    {
        Objects.requireNonNull(tiers, "tiers");
        if(tiers.isEmpty())
        {
            throw new IllegalArgumentException("at least one tier is required");
        }

        List<Tier> copy=new ArrayList<>(tiers);
        // Sort ascending; the open-ended tier (null bound) must sort last.
        copy.sort(Comparator.comparing(Tier::upperBoundInclusive,
                Comparator.nullsLast(Comparator.naturalOrder())));
        long openEnded=copy.stream().filter(t->t.upperBoundInclusive()==null).count();
        if(openEnded!=1)
        {
            throw new IllegalArgumentException(
                    "exactly one open-ended (null upper bound) top tier is required");
        }

        if(copy.get(copy.size()-1).upperBoundInclusive()!=null)
        {
            throw new IllegalArgumentException("the open-ended tier must be the highest tier");
        }

        this.tiers=List.copyOf(copy);
    }

    // The tier table required by the project specification
    public static PriceIncrementRules defaultRules()
    {
        return new PriceIncrementRules(List.of(
                new Tier(new BigDecimal("100"), new BigDecimal("5")),
                new Tier(new BigDecimal("1000"), new BigDecimal("10")),
                new Tier(new BigDecimal("10000"), new BigDecimal("50")),
                new Tier(null, new BigDecimal("100"))
));
    }

    // The step that applies at the given price
    public BigDecimal minIncrement(BigDecimal currentPrice)
    {
        Objects.requireNonNull(currentPrice, "currentPrice");
        if(currentPrice.signum()<0)
        {
            throw new IllegalArgumentException("currentPrice must not be negative: "+currentPrice);
        }

        for(Tier tier:tiers)
        {
            if(tier.upperBoundInclusive()==null
                    ||currentPrice.compareTo(tier.upperBoundInclusive())<=0)
            {
                return tier.increment();
            }
        }

        // Unreachable: the open-ended tier always matches.
        throw new IllegalStateException("no tier matched price "+currentPrice);
    }

    // The smallest amount a new bid must reach: current price plus its step
    public BigDecimal minNextBid(BigDecimal currentPrice)
    {
        return currentPrice.add(minIncrement(currentPrice));
    }
}
