package com.example.portfolio.dto;

import java.math.BigDecimal;

public class GroupContribution {
    private String groupName;
    private BigDecimal contributionPct;
    private String pricingMode;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getContributionPct() {
        return contributionPct;
    }

    public void setContributionPct(BigDecimal contributionPct) {
        this.contributionPct = contributionPct;
    }

    public String getPricingMode() {
        return pricingMode;
    }

    public void setPricingMode(String pricingMode) {
        this.pricingMode = pricingMode;
    }
}
