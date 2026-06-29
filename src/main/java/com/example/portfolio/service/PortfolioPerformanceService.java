package com.example.portfolio.service;

import com.example.portfolio.dto.DailyReturnSummary;
import com.example.portfolio.dto.PortfolioValuationRequest;
import com.example.portfolio.dto.ValidationStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PortfolioPerformanceService {
    private static final Logger logger = LoggerFactory.getLogger(PortfolioPerformanceService.class);
    private static final BigDecimal DEFAULT_TOLERANCE = new BigDecimal("0.005"); // 0.5%
    private static final BigDecimal THRESHOLD_PERCENT = new BigDecimal("5");
    private static final BigDecimal NET_CASH_FLOW_RATIO = new BigDecimal("0.20");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final Set<String> processedTransactionIds = ConcurrentHashMap.newKeySet();

    public DailyReturnSummary calculateDailyReturn(PortfolioValuationRequest req) {
        if (req == null) {
            return invalidSummary("Request cannot be null");
        }

        List<String> errors = validateRequest(req);
        if (!errors.isEmpty()) {
            return invalidSummary(String.join("; ", errors));
        }

        if (isDuplicateTransaction(req.getTransactionId())) {
            return invalidSummary("transactionId is duplicate");
        }
        recordTransactionId(req.getTransactionId());

        BigDecimal beginValue = req.getBeginMarketValue();
        // Log incoming request as JSON for observability
        try {
            ObjectMapper om = new ObjectMapper();
            om.findAndRegisterModules();
            String reqJson = om.writeValueAsString(req);
            logger.info("Incoming PortfolioValuationRequest: {}", reqJson);
        } catch (Exception e) {
            logger.warn("Failed to serialize PortfolioValuationRequest", e);
        }
        BigDecimal endValue = req.getEndMarketValue();
        BigDecimal cash = req.getNetCashFlow() == null ? BigDecimal.ZERO : req.getNetCashFlow();
        BigDecimal benchmarkReturnPct = req.getBenchmarkReturnPct();
        BigDecimal tolerance = req.getTolerance() == null ? DEFAULT_TOLERANCE : req.getTolerance();

        if (beginValue.compareTo(BigDecimal.ZERO) == 0) {
            if (endValue.compareTo(BigDecimal.ZERO) != 0 || cash.compareTo(BigDecimal.ZERO) != 0) {
                return invalidSummary("beginMarketValue is 0 and endMarketValue or netCashFlow is not zero");
            }
        }

        BigDecimal portfolioReturn;
        if (beginValue.compareTo(BigDecimal.ZERO) > 0) {
            portfolioReturn = endValue.subtract(beginValue).subtract(cash)
                    .divide(beginValue, 12, RoundingMode.HALF_UP)
                    .multiply(HUNDRED);
        } else {
            portfolioReturn = BigDecimal.ZERO;
        }

        BigDecimal benchmarkReturnPercent = benchmarkReturnPct.multiply(HUNDRED);
        BigDecimal difference = portfolioReturn.subtract(benchmarkReturnPercent).abs();
        BigDecimal cashFlowRatio = beginValue.compareTo(BigDecimal.ZERO) > 0
                ? cash.abs().divide(beginValue, 12, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        boolean reviewRequired = difference.compareTo(THRESHOLD_PERCENT) > 0
                || cashFlowRatio.compareTo(NET_CASH_FLOW_RATIO) > 0;

        DailyReturnSummary summary = new DailyReturnSummary();
        summary.setValuationDate(req.getValuationDate());
        summary.setDailyReturnPercent(portfolioReturn.setScale(6, RoundingMode.HALF_UP));
        summary.setBenchmarkReturnPercent(benchmarkReturnPercent.setScale(6, RoundingMode.HALF_UP));
        summary.setExcessReturnPercent(portfolioReturn.subtract(benchmarkReturnPercent).setScale(6, RoundingMode.HALF_UP));
        summary.setStatus(reviewRequired ? ValidationStatus.REVIEW_REQUIRED : ValidationStatus.VALID);
        summary.setMessage(reviewRequired ? "REVIEW_REQUIRED" : "VALID");
        return summary;
    }

    private List<String> validateRequest(PortfolioValuationRequest req) {
        List<String> errors = new ArrayList<>();
        if (req.getPortfolioId() == null || req.getPortfolioId().isBlank()) {
            errors.add("portfolioId is required");
        }

        if (req.getCurrency() == null || req.getCurrency().isBlank()) {
            errors.add("currency is required");
        }

        if (req.getBeginMarketValue() == null) {
            errors.add("beginMarketValue is required");
        } else if (req.getBeginMarketValue().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("beginMarketValue must be >= 0");
        }

        if (req.getEndMarketValue() == null) {
            errors.add("endMarketValue is required");
        } else if (req.getEndMarketValue().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("endMarketValue must be >= 0");
        }

        if (req.getBenchmarkReturnPct() == null) {
            errors.add("benchmarkReturnPct is required");
        }

        if (req.getAmount() == null) {
            errors.add("amount is required");
        } else if (req.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("amount must be >= 0");
        }

        if (req.getSequenceNumber() == null) {
            errors.add("sequenceNumber is required");
        } else if (req.getSequenceNumber() < 0) {
            errors.add("sequenceNumber must be >= 0");
        }

        if (req.getRequestId() == null || req.getRequestId().isBlank()) {
            errors.add("requestId is required");
        }

        if (req.getTransactionId() == null || req.getTransactionId().isBlank()) {
            errors.add("transactionId is required");
        }

        if (req.getRequestedBy() == null || req.getRequestedBy().isBlank()) {
            errors.add("requestedBy is required");
        }

        if (req.getAccountId() == null || req.getAccountId().isBlank()) {
            errors.add("accountId is required");
        }

        if (req.getTransactionType() == null || req.getTransactionType().isBlank()) {
            errors.add("transactionType is required");
        }

        if (req.getNetCashFlow() != null && req.getNetCashFlow().abs().compareTo(new BigDecimal("1E20")) > 0) {
            errors.add("netCashFlow out of range");
        }

        return errors;
    }

    private DailyReturnSummary invalidSummary(String reason) {
        DailyReturnSummary summary = new DailyReturnSummary();
        summary.setStatus(ValidationStatus.INVALID_INPUT);
        summary.setMessage(reason);
        return summary;
    }

    private boolean isDuplicateTransaction(String transactionId) {
        return transactionId != null && processedTransactionIds.contains(transactionId);
    }

    private void recordTransactionId(String transactionId) {
        if (transactionId != null) {
            processedTransactionIds.add(transactionId);
        }
    }
}
