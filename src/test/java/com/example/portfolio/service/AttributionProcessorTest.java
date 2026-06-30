package com.example.portfolio.service;

import com.example.portfolio.dto.AttributionGroup;
import com.example.portfolio.dto.AttributionSummary;
import com.example.portfolio.dto.PortfolioAttributionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttributionProcessorTest {

    private AttributionProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new AttributionProcessor(new FallbackPricingService());
    }

    @Test
    void validPrimaryPricingReturnsExpectedSummary() {
        PortfolioAttributionRequest req = new PortfolioAttributionRequest();
        req.setPortfolioId("PORT1");
        req.setRequestId("REQ-20001");
        req.setCurrency("USD");
        req.setRequestedBy("advisor02");
        req.setValuationDate(LocalDate.of(2026, 6, 28));

        AttributionGroup equity = new AttributionGroup();
        equity.setGroupName("equity");
        equity.setWeightPct(new BigDecimal("60"));
        equity.setReturnPct(new BigDecimal("1.5"));

        AttributionGroup fixedIncome = new AttributionGroup();
        fixedIncome.setGroupName("fixed income");
        fixedIncome.setWeightPct(new BigDecimal("40"));
        fixedIncome.setReturnPct(new BigDecimal("0.75"));

        req.setGroups(List.of(equity, fixedIncome));

        AttributionSummary summary = processor.process(req);

        assertEquals("VALID", summary.getStatus());
        assertFalse(summary.isDegraded());
        assertEquals(new BigDecimal("1.050000"), summary.getTotalContributionPct());
        assertEquals("PRIMARY", summary.getGroupContributions().get(0).getPricingMode());
        assertEquals("PRIMARY", summary.getGroupContributions().get(1).getPricingMode());
    }

    @Test
    void degradedWhenOneGroupMissingReturnWithoutFallback() {
        PortfolioAttributionRequest req = new PortfolioAttributionRequest();
        req.setPortfolioId("PORT1");
        req.setRequestId("REQ-20002");
        req.setCurrency("USD");
        req.setRequestedBy("advisor02");
        req.setValuationDate(LocalDate.of(2026, 6, 28));

        AttributionGroup group = new AttributionGroup();
        group.setGroupName("real estate");
        group.setWeightPct(new BigDecimal("100"));
        group.setReturnPct(null);

        req.setGroups(List.of(group));

        AttributionSummary summary = processor.process(req);

        assertEquals("DEGRADED", summary.getStatus());
        assertTrue(summary.isDegraded());
        assertEquals("NO_FALLBACK", summary.getGroupContributions().get(0).getPricingMode());
    }

    @Test
    void reviewRequiredWhenMultipleGroupsMissingReturnWithoutFallback() {
        PortfolioAttributionRequest req = new PortfolioAttributionRequest();
        req.setPortfolioId("PORT1");
        req.setRequestId("REQ-20003");
        req.setCurrency("USD");
        req.setRequestedBy("advisor02");
        req.setValuationDate(LocalDate.of(2026, 6, 28));

        AttributionGroup groupA = new AttributionGroup();
        groupA.setGroupName("real estate");
        groupA.setWeightPct(new BigDecimal("50"));
        groupA.setReturnPct(null);

        AttributionGroup groupB = new AttributionGroup();
        groupB.setGroupName("commodities");
        groupB.setWeightPct(new BigDecimal("50"));
        groupB.setReturnPct(null);

        req.setGroups(List.of(groupA, groupB));

        AttributionSummary summary = processor.process(req);

        assertEquals("REVIEW_REQUIRED", summary.getStatus());
        assertTrue(summary.isDegraded());
        assertEquals(2, summary.getWarnings().size());
    }
}
