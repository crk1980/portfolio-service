package com.example.portfolio.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class FallbackPricingServiceTest {

    private final FallbackPricingService fallbackPricingService = new FallbackPricingService();

    @Test
    void returnsKnownFallbackReturnsForSupportedGroups() {
        assertEquals(new BigDecimal("0.45"), fallbackPricingService.getFallbackReturnPct("Cash"));
        assertEquals(new BigDecimal("0.75"), fallbackPricingService.getFallbackReturnPct("fixed income"));
        assertEquals(new BigDecimal("1.50"), fallbackPricingService.getFallbackReturnPct("equity"));
    }

    @Test
    void returnsNullForUnsupportedGroup() {
        assertNull(fallbackPricingService.getFallbackReturnPct("real estate"));
    }
}
