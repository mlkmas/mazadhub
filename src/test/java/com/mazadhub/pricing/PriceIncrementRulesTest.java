package com.mazadhub.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceIncrementRulesTest {

    private final PriceIncrementRules rules = PriceIncrementRules.defaultRules();

    @ParameterizedTest(name = "price {0} -> increment {1}")
    @CsvSource({
            "0,     5",
            "50,    5",
            "100,   5",      // boundary: 'up to 100' is inclusive
            "100.01,10",
            "500,   10",
            "1000,  10",     // boundary
            "1000.5,50",
            "5000,  50",
            "10000, 50",     // boundary
            "10000.01, 100",
            "25000, 100"
    })
    void minIncrement_followsTiers(String price, String expected) {
        assertEquals(new BigDecimal(expected), rules.minIncrement(new BigDecimal(price)));
    }

    @Test
    void minNextBid_addsIncrementToCurrentPrice() {
        assertEquals(new BigDecimal("105"), rules.minNextBid(new BigDecimal("100")));
        assertEquals(new BigDecimal("510"), rules.minNextBid(new BigDecimal("500")));
        assertEquals(new BigDecimal("25100"), rules.minNextBid(new BigDecimal("25000")));
    }

    @Test
    void negativePrice_isRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> rules.minIncrement(new BigDecimal("-1")));
    }

    @Test
    void construction_requiresExactlyOneOpenEndedTopTier() {
        // No open-ended tier:
        assertThrows(IllegalArgumentException.class, () -> new PriceIncrementRules(List.of(
                new PriceIncrementRules.Tier(new BigDecimal("100"), new BigDecimal("5")))));
        // Two open-ended tiers:
        assertThrows(IllegalArgumentException.class, () -> new PriceIncrementRules(List.of(
                new PriceIncrementRules.Tier(null, new BigDecimal("5")),
                new PriceIncrementRules.Tier(null, new BigDecimal("10")))));
    }

    @Test
    void tiers_areSortedRegardlessOfInputOrder() {
        PriceIncrementRules shuffled = new PriceIncrementRules(List.of(
                new PriceIncrementRules.Tier(null,                   new BigDecimal("100")),
                new PriceIncrementRules.Tier(new BigDecimal("1000"), new BigDecimal("10")),
                new PriceIncrementRules.Tier(new BigDecimal("100"),  new BigDecimal("5"))
        ));
        assertEquals(new BigDecimal("5"), shuffled.minIncrement(new BigDecimal("50")));
        assertEquals(new BigDecimal("10"), shuffled.minIncrement(new BigDecimal("500")));
        assertEquals(new BigDecimal("100"), shuffled.minIncrement(new BigDecimal("99999")));
    }
}
