package com.example.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PortfolioAttributionRequest {
    @NotBlank(message = "portfolioId is required")
    private String portfolioId;

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    private LocalDate valuationDate;

    @NotBlank(message = "requestId is required")
    private String requestId;

    @NotBlank(message = "transactionId is required")
    private String transactionId;

    @NotNull(message = "sequenceNumber is required")
    private Long sequenceNumber;

    @NotBlank(message = "accountId is required")
    private String accountId;

    @NotBlank(message = "transactionType is required")
    private String transactionType;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "amount must be >= 0")
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    private String currency;

    @NotBlank(message = "requestedBy is required")
    private String requestedBy;

    @NotNull(message = "groups are required")
    @NotEmpty(message = "groups cannot be empty")
    @Valid
    private List<AttributionGroup> groups;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
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

    public List<AttributionGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<AttributionGroup> groups) {
        this.groups = groups;
    }
}
