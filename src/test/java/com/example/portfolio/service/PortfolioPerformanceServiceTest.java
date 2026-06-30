package com.example.portfolio.service;

import com.example.portfolio.dto.AttributionGroup;
import com.example.portfolio.dto.AttributionSummary;
import com.example.portfolio.dto.DailyReturnSummary;
import com.example.portfolio.dto.PortfolioAttributionRequest;
import com.example.portfolio.dto.PortfolioValuationRequest;
import com.example.portfolio.dto.ValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioPerformanceServiceTest {

    private PortfolioPerformanceService service;

    @BeforeEach
    void setUp() {
        service = new PortfolioPerformanceService(
                new AttributionProcessor(new FallbackPricingService()),
                new AttributionValidator());
    }

    private PortfolioValuationRequest createBaseRequest() {
        PortfolioValuationRequest req = new PortfolioValuationRequest();
        req.setPortfolioId("PORT1");
        req.setRequestId("REQ-12345");
        req.setTransactionId("TXN-98765");
        req.setAccountId("ACC-001");
        req.setTransactionType("DEPOSIT");
        req.setCurrency("USD");
        req.setRequestedBy("investor@example.com");
        req.setSequenceNumber(1L);
        req.setAmount(new BigDecimal("1000"));
        req.setValuationDate(LocalDate.now());
        return req;
    }

    private PortfolioAttributionRequest createBaseAttributionRequest(String requestId, String transactionId) {
        PortfolioAttributionRequest req = new PortfolioAttributionRequest();
        req.setPortfolioId("PORT1");
        req.setRequestId(requestId);
        req.setTransactionId(transactionId);
        req.setSequenceNumber(1L);
        req.setAccountId("ACC-201");
        req.setTransactionType("DEBIT");
        req.setAmount(new BigDecimal("40"));
        req.setCurrency("USD");
        req.setRequestedBy("advisor02");
        req.setValuationDate(LocalDate.of(2026, 6, 28));
        return req;
    }

    // Verify valid daily return calculation and expected percentage output when all inputs are correct.
    @Test
    void calculateDailyReturn_valid() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setBeginMarketValue(new BigDecimal("100000"));
        req.setEndMarketValue(new BigDecimal("100800"));
        req.setNetCashFlow(BigDecimal.ZERO);
        req.setBenchmarkReturnPct(new BigDecimal("0.007"));
        req.setTolerance(new BigDecimal("0.005"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertNotNull(s);
        assertEquals(ValidationStatus.VALID, s.getStatus());
        assertEquals(new BigDecimal("0.800000"), s.getDailyReturnPercent());
        assertEquals(new BigDecimal("0.700000"), s.getBenchmarkReturnPercent());
        assertEquals(new BigDecimal("0.100000"), s.getExcessReturnPercent());
        assertEquals("VALID", s.getMessage());
    }

    // Ensure input with negative begin or end market value is rejected as invalid input.
    @Test
    void rejectInvalidForNegativeBeginOrEndMarketValue() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setBeginMarketValue(new BigDecimal("-1"));
        req.setEndMarketValue(new BigDecimal("1000"));
        req.setBenchmarkReturnPct(new BigDecimal("0.001"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertEquals(ValidationStatus.INVALID_INPUT, s.getStatus());
        assertTrue(s.getMessage().contains("beginMarketValue must be >= 0"));

        req = createBaseRequest();
        req.setBeginMarketValue(new BigDecimal("1000"));
        req.setEndMarketValue(new BigDecimal("-1"));
        req.setBenchmarkReturnPct(new BigDecimal("0.001"));

        s = service.calculateDailyReturn(req);
        assertEquals(ValidationStatus.INVALID_INPUT, s.getStatus());
        assertTrue(s.getMessage().contains("endMarketValue must be >= 0"));
    }

    // Ensure missing currency is rejected with INVALID_INPUT and a descriptive message.
    @Test
    void rejectInvalidWhenCurrencyMissing() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setCurrency(" ");
        req.setBeginMarketValue(new BigDecimal("100000"));
        req.setEndMarketValue(new BigDecimal("100800"));
        req.setBenchmarkReturnPct(new BigDecimal("0.007"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertEquals(ValidationStatus.INVALID_INPUT, s.getStatus());
        assertTrue(s.getMessage().contains("currency is required"));
    }

    // Reject cases where beginMarketValue is zero but endMarketValue or netCashFlow is non-zero.
    @Test
    void rejectInvalidWhenBeginZeroAndEndNotZero() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setBeginMarketValue(BigDecimal.ZERO);
        req.setEndMarketValue(new BigDecimal("1000"));
        req.setNetCashFlow(BigDecimal.ZERO);
        req.setBenchmarkReturnPct(new BigDecimal("0.01"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertEquals(ValidationStatus.INVALID_INPUT, s.getStatus());
        assertTrue(s.getMessage().contains("beginMarketValue is 0 and endMarketValue or netCashFlow is not zero"));
    }

    // Verify REVIEW_REQUIRED when portfolio return difference exceeds the tolerance threshold.
    @Test
    void reviewRequiredWhenDifferenceExceedsFivePercent() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setBeginMarketValue(new BigDecimal("100000"));
        req.setEndMarketValue(new BigDecimal("106000"));
        req.setNetCashFlow(BigDecimal.ZERO);
        req.setBenchmarkReturnPct(new BigDecimal("0.001"));
        req.setTolerance(new BigDecimal("0.005"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertEquals(ValidationStatus.REVIEW_REQUIRED, s.getStatus());
        assertEquals(new BigDecimal("6.000000"), s.getDailyReturnPercent());
        assertEquals(new BigDecimal("5.900000"), s.getExcessReturnPercent());
    }

    // Verify REVIEW_REQUIRED when net cash flow is more than 20% of the begin market value.
    @Test
    void reviewRequiredWhenNetCashFlowOver20PercentOfBeginValue() {
        PortfolioValuationRequest req = createBaseRequest();
        req.setBeginMarketValue(new BigDecimal("100000"));
        req.setEndMarketValue(new BigDecimal("100000"));
        req.setNetCashFlow(new BigDecimal("25000"));
        req.setBenchmarkReturnPct(BigDecimal.ZERO);
        req.setTolerance(new BigDecimal("0.005"));

        DailyReturnSummary s = service.calculateDailyReturn(req);

        assertEquals(ValidationStatus.REVIEW_REQUIRED, s.getStatus());
        assertTrue(s.getExcessReturnPercent().abs().compareTo(new BigDecimal("20.000000")) > 0);
    }

    // Confirm duplicate transactionIds are rejected after the first processed request.
    @Test
    void rejectDuplicateTransactionId() {
        PortfolioValuationRequest first = createBaseRequest();
        first.setBeginMarketValue(new BigDecimal("100000"));
        first.setEndMarketValue(new BigDecimal("100800"));
        first.setNetCashFlow(BigDecimal.ZERO);
        first.setBenchmarkReturnPct(new BigDecimal("0.007"));
        first.setTolerance(new BigDecimal("0.005"));

        DailyReturnSummary firstSummary = service.calculateDailyReturn(first);
        assertEquals(ValidationStatus.VALID, firstSummary.getStatus());

        PortfolioValuationRequest duplicate = createBaseRequest();
        duplicate.setBeginMarketValue(new BigDecimal("100000"));
        duplicate.setEndMarketValue(new BigDecimal("100900"));
        duplicate.setNetCashFlow(BigDecimal.ZERO);
        duplicate.setBenchmarkReturnPct(new BigDecimal("0.009"));
        duplicate.setTolerance(new BigDecimal("0.005"));

        DailyReturnSummary duplicateSummary = service.calculateDailyReturn(duplicate);
        assertEquals(ValidationStatus.INVALID_INPUT, duplicateSummary.getStatus());
        assertTrue(duplicateSummary.getMessage().contains("transactionId is duplicate"));
    }

    // Test valid attribution processing using primary pricing for all groups.
    @Test
    void calculateAttribution_validPrimaryPricing() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12345", "TXN-1001");

        AttributionGroup equity = new AttributionGroup();
        equity.setGroupName("equity");
        equity.setWeightPct(new BigDecimal("60"));
        equity.setReturnPct(new BigDecimal("1.5"));

        AttributionGroup other = new AttributionGroup();
        other.setGroupName("other");
        other.setWeightPct(new BigDecimal("40"));
        other.setReturnPct(BigDecimal.ZERO);

        req.setGroups(List.of(equity, other));

        AttributionSummary summary = service.calculateAttribution(req);

        assertEquals("PORT1", summary.getPortfolioId());
        assertEquals("REQ-12345", summary.getRequestId());
        assertEquals(new BigDecimal("0.900000"), summary.getGroupContributions().get(0).getContributionPct());
        assertEquals("PRIMARY", summary.getGroupContributions().get(0).getPricingMode());
        assertEquals(new BigDecimal("0.900000"), summary.getTotalContributionPct());
        assertFalse(summary.isDegraded());
        assertTrue(summary.getWarnings().isEmpty());
        assertEquals("VALID", summary.getStatus());
    }

    // Test fallback pricing when a group is missing returnPct and fallback data exists.
    @Test
    void calculateAttribution_withFallbackPricing() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12345", "TXN-1002");

        AttributionGroup cash = new AttributionGroup();
        cash.setGroupName("Cash");
        cash.setWeightPct(new BigDecimal("40"));
        cash.setReturnPct(null);

        AttributionGroup zeroReturn = new AttributionGroup();
        zeroReturn.setGroupName("other");
        zeroReturn.setWeightPct(new BigDecimal("60"));
        zeroReturn.setReturnPct(BigDecimal.ZERO);

        req.setGroups(List.of(cash, zeroReturn));

        AttributionSummary summary = service.calculateAttribution(req);

        assertEquals(new BigDecimal("0.180000"), summary.getGroupContributions().get(0).getContributionPct());
        assertEquals("FALLBACK_USED", summary.getGroupContributions().get(0).getPricingMode());
        assertTrue(summary.isDegraded());
        assertEquals(1, summary.getWarnings().size());
        assertTrue(summary.getWarnings().get(0).contains("Fallback return used for Cash"));
    }

    // Ensure invalid total group weight is rejected with INVALID_INPUT.
    @Test
    void rejectAttributionWhenTotalWeightOutsideRange() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12346", "TXN-1003");

        AttributionGroup equity = new AttributionGroup();
        equity.setGroupName("equity");
        equity.setWeightPct(new BigDecimal("98"));
        equity.setReturnPct(new BigDecimal("1.5"));

        req.setGroups(List.of(equity));

        AttributionSummary summary = service.calculateAttribution(req);

        assertEquals("INVALID_INPUT", summary.getStatus());
        assertTrue(summary.getWarnings().get(0).contains("Total group weight must be between 99 and 101 percent"));
    }

    // Test degraded attribution when a missing returnPct has no fallback available.
    @Test
    void calculateAttribution_degradedWhenMissingReturnNoFallback() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12347", "TXN-1004");

        AttributionGroup other = new AttributionGroup();
        other.setGroupName("real estate");
        other.setWeightPct(new BigDecimal("100"));
        other.setReturnPct(null);

        req.setGroups(List.of(other));

        AttributionSummary summary = service.calculateAttribution(req);

        assertEquals("DEGRADED", summary.getStatus());
        assertTrue(summary.isDegraded());
        assertEquals("NO_FALLBACK", summary.getGroupContributions().get(0).getPricingMode());
        assertTrue(summary.getWarnings().get(0).contains("Missing returnPct and no fallback available for real estate"));
    }

    // Test REVIEW_REQUIRED when multiple groups are missing returns and no fallback is available.
    @Test
    void calculateAttribution_reviewRequiredWhenMultipleMissingReturnsNoFallback() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12348", "TXN-1005");

        AttributionGroup groupA = new AttributionGroup();
        groupA.setGroupName("real estate");
        groupA.setWeightPct(new BigDecimal("50"));
        groupA.setReturnPct(null);

        AttributionGroup groupB = new AttributionGroup();
        groupB.setGroupName("commodities");
        groupB.setWeightPct(new BigDecimal("50"));
        groupB.setReturnPct(null);

        req.setGroups(List.of(groupA, groupB));

        AttributionSummary summary = service.calculateAttribution(req);

        assertEquals("REVIEW_REQUIRED", summary.getStatus());
        assertTrue(summary.isDegraded());
        assertEquals(2, summary.getWarnings().size());
        assertTrue(summary.getWarnings().stream().anyMatch(w -> w.contains("real estate")));
        assertTrue(summary.getWarnings().stream().anyMatch(w -> w.contains("commodities")));
    }

    // Verify idempotent behavior: repeated calls with the same requestId return the same cached attribution.
    @Test
    void calculateAttribution_isIdempotentForSameRequestId() {
        PortfolioAttributionRequest req = createBaseAttributionRequest("REQ-12349", "TXN-1006");

        AttributionGroup equity = new AttributionGroup();
        equity.setGroupName("equity");
        equity.setWeightPct(new BigDecimal("60"));
        equity.setReturnPct(new BigDecimal("1.5"));

        AttributionGroup other = new AttributionGroup();
        other.setGroupName("other");
        other.setWeightPct(new BigDecimal("40"));
        other.setReturnPct(BigDecimal.ZERO);

        req.setGroups(List.of(equity, other));

        AttributionSummary first = service.calculateAttribution(req);

        PortfolioAttributionRequest duplicateRequest = createBaseAttributionRequest("REQ-12349", "TXN-1007");

        AttributionGroup altered = new AttributionGroup();
        altered.setGroupName("cash");
        altered.setWeightPct(new BigDecimal("40"));
        altered.setReturnPct(new BigDecimal("0.5"));
        duplicateRequest.setGroups(List.of(altered));

        AttributionSummary second = service.calculateAttribution(duplicateRequest);

        //assertSame(first, second);
        assertEquals(first.getGroupContributions().get(0).getGroupName(), second.getGroupContributions().get(0).getGroupName());
        assertEquals(first.getTotalContributionPct(), second.getTotalContributionPct());
    }
}
