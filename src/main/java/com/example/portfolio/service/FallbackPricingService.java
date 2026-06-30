package com.example.portfolio.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FallbackPricingService {
    public BigDecimal getFallbackReturnPct(String groupName) {
        if (groupName == null) {
            return null;
        }

        String normalized = groupName.trim().toLowerCase();
        return switch (normalized) {
            case "cash" -> new BigDecimal("0.45");
            case "fixed income", "fixed-income", "fixed_income" -> new BigDecimal("0.75");
            case "equity" -> new BigDecimal("1.50");
            default -> null;
        };
    }

    public boolean hasFallback(String groupName) {
        return getFallbackReturnPct(groupName) != null;
    }
}
