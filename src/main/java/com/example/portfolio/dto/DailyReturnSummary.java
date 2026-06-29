package com.example.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DailyReturnSummary {
    private LocalDate valuationDate;
    private BigDecimal dailyReturnPercent;
    private BigDecimal benchmarkReturnPercent;
    private BigDecimal excessReturnPercent;
    private ValidationStatus status;
    private String message;

    public LocalDate getValuationDate() {
        return valuationDate;
    }

    public void setValuationDate(LocalDate valuationDate) {
        this.valuationDate = valuationDate;
    }

    public BigDecimal getDailyReturnPercent() {
        return dailyReturnPercent;
    }

    public void setDailyReturnPercent(BigDecimal dailyReturnPercent) {
        this.dailyReturnPercent = dailyReturnPercent;
    }

    public BigDecimal getBenchmarkReturnPercent() {
        return benchmarkReturnPercent;
    }

    public void setBenchmarkReturnPercent(BigDecimal benchmarkReturnPercent) {
        this.benchmarkReturnPercent = benchmarkReturnPercent;
    }

    public BigDecimal getExcessReturnPercent() {
        return excessReturnPercent;
    }

    public void setExcessReturnPercent(BigDecimal excessReturnPercent) {
        this.excessReturnPercent = excessReturnPercent;
    }

    public ValidationStatus getStatus() {
        return status;
    }

    public void setStatus(ValidationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
