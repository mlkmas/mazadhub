package com.mazadhub.pricing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Computes the minimum increment between consecutive bids, based on a
 * configurable table of price tiers.
 *
 * <p>The default table follows the project specification:
 * <ul>
 *   <li>up to 100      &rarr; increment 5</li>
 *   <li>100 .. 1000    &rarr; increment 10</li>
 *   <li>1000 .. 10000  &rarr; increment 50</li>
 *   <li>above 10000    &rarr; increment 100</li>
 * </ul>

 */
public final class PriceIncrementRules
{

    public record Tier(BigDecimal upperBoundInclusive, BigDecimal increment)
    {
        public Tier
        {
            Objects.requireNonNull(increment, "increment");
            if (increment.signum() <= 0) {
                throw new IllegalArgumentException("increment must be positive: " + increment);
            }
            if (upperBoundInclusive != null && upperBoundInclusive.signum() <= 0)
            {
                throw new IllegalArgumentException("upper bound must be positive: " + upperBoundInclusive);
            }
        }
    }

    private final List<Tier> tiers;

    public PriceIncrementRules(List<Tier> tiers)
    {
        Objects.requireNonNull(tiers, "tiers");
        if (tiers.isEmpty()) {
            throw new IllegalArgumentException("at least one tier is required");
        }
        List<Tier> copy = new ArrayList<>(tiers);
        // Sort ascending; the open-ended tier (null bound) must sort last.
        copy.sort(Comparator.comparing(Tier::upperBoundInclusive,
                Comparator.nullsLast(Comparator.naturalOrder())));
        long openEnded = copy.stream().filter(t -> t.upperBoundInclusive() == null).count();
        if (openEnded != 1) {
            throw new IllegalArgumentException(
                    "exactly one open-ended (null upper bound) top tier is required");
        }
        if (copy.get(copy.size() - 1).upperBoundInclusive() != null) {
            throw new IllegalArgumentException("the open-ended tier must be the highest tier");
        }
        this.tiers = List.copyOf(copy);
    }

    /** The project-default tier table. */
    public static PriceIncrementRules defaultRules() {
        return new PriceIncrementRules(List.of(
                new Tier(new BigDecimal("100"),   new BigDecimal("5")),
                new Tier(new BigDecimal("1000"),  new BigDecimal("10")),
                new Tier(new BigDecimal("10000"), new BigDecimal("50")),
                new Tier(null,                    new BigDecimal("100"))
        ));
    }

    /**
     * Returns the minimum increment that applies to the given current price.
     *
     * @param currentPrice the current price of the item (must be &gt;= 0)
     * @return the minimum increment for that price tier
     */
    public BigDecimal minIncrement(BigDecimal currentPrice) {
        Objects.requireNonNull(currentPrice, "currentPrice");
        if (currentPrice.signum() < 0) {
            throw new IllegalArgumentException("currentPrice must not be negative: " + currentPrice);
        }
        for (Tier tier : tiers) {
            if (tier.upperBoundInclusive() == null
                    || currentPrice.compareTo(tier.upperBoundInclusive()) <= 0) {
                return tier.increment();
            }
        }
        // Unreachable: the open-ended tier always matches.
        throw new IllegalStateException("no tier matched price " + currentPrice);
    }

    /**
     * Returns the smallest amount a new bid must reach to be valid
     */
    public BigDecimal minNextBid(BigDecimal currentPrice)
    {
        return currentPrice.add(minIncrement(currentPrice));
    }
}
