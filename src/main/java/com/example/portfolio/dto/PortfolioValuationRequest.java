package com.example.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class PortfolioValuationRequest {
    @NotBlank(message = "portfolioId is required")
    private String portfolioId;

    private LocalDate valuationDate;

    @NotNull(message = "beginMarketValue is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "beginMarketValue must be >= 0")
    private BigDecimal beginMarketValue;

    @NotNull(message = "endMarketValue is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "endMarketValue must be >= 0")
    private BigDecimal endMarketValue;

    private BigDecimal netCashFlow;

    @NotNull(message = "benchmarkReturnPct is required")
    private BigDecimal benchmarkReturnPct; // expressed as decimal (e.g., 0.01 = 1%)

    private BigDecimal tolerance; // absolute tolerance as decimal (e.g., 0.005 = 0.5%)

    @NotBlank(message = "currency is required")
    private String currency;

    @NotBlank(message = "requestedBy is required")
    private String requestedBy;

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotBlank(message = "accountId is required")
    private String accountId;

    @NotBlank(message = "transactionType is required")
    private String transactionType;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be >= 0")
    private BigDecimal amount;

    @NotNull(message = "sequenceNumber is required")
    private Long sequenceNumber;

    @NotBlank(message = "requestId is required")
    private String requestId;

    private String status;

    public String getPortfolioId() {
        return portfolioId;
    }

    public void setPortfolioId(String portfolioId) {
        this.portfolioId = portfolioId;
    }

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public BigDecimal getBeginMarketValue() {
        return beginMarketValue;
    }

    public void setBeginMarketValue(BigDecimal beginMarketValue) {
        this.beginMarketValue = beginMarketValue;
    }

    public BigDecimal getEndMarketValue() {
        return endMarketValue;
    }

    public void setEndMarketValue(BigDecimal endMarketValue) {
        this.endMarketValue = endMarketValue;
    }

    public BigDecimal getNetCashFlow() {
        return netCashFlow;
    }

    public void setNetCashFlow(BigDecimal netCashFlow) {
        this.netCashFlow = netCashFlow;
    }

    public BigDecimal getBenchmarkReturnPct() {
        return benchmarkReturnPct;
    }

    public void setBenchmarkReturnPct(BigDecimal benchmarkReturnPct) {
        this.benchmarkReturnPct = benchmarkReturnPct;
    }

    public BigDecimal getTolerance() {
        return tolerance;
    }

    public void setTolerance(BigDecimal tolerance) {
        this.tolerance = tolerance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
