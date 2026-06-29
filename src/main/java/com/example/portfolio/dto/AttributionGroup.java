package com.example.portfolio.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AttributionGroup {
    @NotBlank(message = "groupName is required")
    private String groupName;

    @NotNull(message = "weightPct is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "weightPct must be >= 0")
    private BigDecimal weightPct;

    private BigDecimal returnPct;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public BigDecimal getWeightPct() {
        return weightPct;
    }

    public void setWeightPct(BigDecimal weightPct) {
        this.weightPct = weightPct;
    }

    public BigDecimal getReturnPct() {
        return returnPct;
    }

    public void setReturnPct(BigDecimal returnPct) {
        this.returnPct = returnPct;
    }
}
