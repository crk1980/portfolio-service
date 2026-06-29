package com.example.portfolio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AttributionSummary {
    private String portfolioId;
    private LocalDate valuationDate;
    private String requestId;
    private BigDecimal totalContributionPct;
    private List<GroupContribution> groupContributions;
    private String status;
    private boolean degraded;
    private List<String> warnings;
    private String processedAt;

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

    public BigDecimal getTotalContributionPct() {
        return totalContributionPct;
    }

    public void setTotalContributionPct(BigDecimal totalContributionPct) {
        this.totalContributionPct = totalContributionPct;
    }

    public List<GroupContribution> getGroupContributions() {
        return groupContributions;
    }

    public void setGroupContributions(List<GroupContribution> groupContributions) {
        this.groupContributions = groupContributions;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isDegraded() {
        return degraded;
    }

    public void setDegraded(boolean degraded) {
        this.degraded = degraded;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public String getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(String processedAt) {
        this.processedAt = processedAt;
    }
}
