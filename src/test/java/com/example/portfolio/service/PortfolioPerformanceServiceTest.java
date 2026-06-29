package com.example.portfolio.service;

import com.example.portfolio.dto.DailyReturnSummary;
import com.example.portfolio.dto.PortfolioValuationRequest;
import com.example.portfolio.dto.ValidationStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioPerformanceServiceTest {

    private final PortfolioPerformanceService service = new PortfolioPerformanceService();

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
}
