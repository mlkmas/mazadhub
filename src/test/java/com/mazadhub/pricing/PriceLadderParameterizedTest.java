package com.mazadhub.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Sweeps the whole price ladder, especially the tier bounds and the values just either side of them
class PriceLadderParameterizedTest
{
    private final PriceIncrementRules rules=PriceIncrementRules.defaultRules();

    @ParameterizedTest(name="price {0} -> increment {1}")
    @CsvSource({
            // --- first tier: <= 100 -> 5 ---
            "0, 5", "0.01, 5", "1, 5", "5, 5", "17, 5", "50, 5", "99, 5",
            "99.99, 5", "100, 5",
            // --- boundary crossing into tier 2 ---
            "100.01, 10", "101, 10", "150, 10", "500, 10", "999, 10",
            "999.99, 10", "1000, 10",
            // --- boundary crossing into tier 3 ---
            "1000.01, 50", "1001, 50", "2500, 50", "5000, 50", "9999, 50",
            "9999.99, 50", "10000, 50",
            // --- open-ended top tier ---
            "10000.01, 100", "10001, 100", "25000, 100", "100000, 100",
            "999999, 100", "1000000000, 100"
    })
    // About 30 prices across the four tiers all return the expected step
    void incrementForPrice(String price, String expectedIncrement)
    {
        assertEquals(0, rules.minIncrement(new BigDecimal(price))
                        .compareTo(new BigDecimal(expectedIncrement)),
                "wrong increment for price "+price);
    }

    @ParameterizedTest(name="price {0} -> min next bid {1}")
    @CsvSource({
            "0, 5", "10, 15", "95, 100", "100, 105",
            "101, 111", "500, 510", "1000, 1010",
            "1001, 1051", "5000, 5050", "10000, 10050",
            "10001, 10101", "50000, 50100", "340, 350"
    })
    // The same prices produce the expected minimum next bid
    void minimumNextBid(String price, String expectedNext)
    {
        assertEquals(0, rules.minNextBid(new BigDecimal(price))
                        .compareTo(new BigDecimal(expectedNext)),
                "wrong minimum next bid for price "+price);
    }

    // Every negative price is refused
    @ParameterizedTest(name="negative price {0} is rejected")
    @ValueSource(strings={"-0.01", "-1", "-100", "-999999"})
    void negativePricesAreRejected(String price)
    {
        assertThrows(IllegalArgumentException.class,
                ()->rules.minIncrement(new BigDecimal(price)));
    }

    // Across a wide sweep of prices the ladder always moves forward
    @Test
    void minNextBidIsAlwaysStrictlyGreaterThanTheCurrentPrice()
    {
        // Sweep a wide range of prices and assert the ladder always moves forward.
        for(long p=0; p<=200_000; p+=137)
        {
            BigDecimal price=BigDecimal.valueOf(p);
            assertTrue(rules.minNextBid(price).compareTo(price)>0,
                    "minNextBid must exceed the price, failed at "+price);
        }
    }

    // No price can ever produce a zero or negative step
    @Test
    void incrementIsNeverZeroOrNegative()
    {
        for(long p=0; p<=200_000; p+=311)
        {
            BigDecimal inc=rules.minIncrement(BigDecimal.valueOf(p));
            assertTrue(inc.signum()>0, "increment must be positive, failed at "+p);
        }
    }
}
