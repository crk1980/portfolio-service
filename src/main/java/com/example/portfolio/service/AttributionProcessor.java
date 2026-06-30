package com.example.portfolio.service;

import com.example.portfolio.dto.AttributionGroup;
import com.example.portfolio.dto.AttributionSummary;
import com.example.portfolio.dto.GroupContribution;
import com.example.portfolio.dto.PortfolioAttributionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AttributionProcessor {
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private final FallbackPricingService fallbackPricingService;

    public AttributionProcessor(FallbackPricingService fallbackPricingService) {
        this.fallbackPricingService = fallbackPricingService;
    }

    public AttributionSummary process(PortfolioAttributionRequest req) {
        AttributionSummary summary = new AttributionSummary();
        List<GroupContribution> contributions = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        BigDecimal totalContribution = BigDecimal.ZERO;
        int missingWithoutFallback = 0;

        for (AttributionGroup group : req.getGroups()) {
            BigDecimal returnPct = group.getReturnPct();
            String pricingMode = "PRIMARY";

            if (returnPct == null) {
                BigDecimal fallbackReturn = fallbackPricingService.getFallbackReturnPct(group.getGroupName());
                if (fallbackReturn != null) {
                    pricingMode = "FALLBACK_USED";
                    returnPct = fallbackReturn;
                    warnings.add("Fallback return used for " + group.getGroupName());
                } else {
                    pricingMode = "NO_FALLBACK";
                    returnPct = BigDecimal.ZERO;
                    warnings.add("Missing returnPct and no fallback available for " + group.getGroupName());
                    missingWithoutFallback++;
                }
            }

            BigDecimal contribution = group.getWeightPct()
                    .multiply(returnPct)
                    .divide(HUNDRED, 12, RoundingMode.HALF_UP);

            GroupContribution contributionSummary = new GroupContribution();
            contributionSummary.setGroupName(group.getGroupName());
            contributionSummary.setContributionPct(contribution.setScale(6, RoundingMode.HALF_UP));
            contributionSummary.setPricingMode(pricingMode);
            contributions.add(contributionSummary);
            totalContribution = totalContribution.add(contribution);
        }

        String overallStatus = "VALID";
        if (missingWithoutFallback == 1) {
            overallStatus = "DEGRADED";
        } else if (missingWithoutFallback > 1) {
            overallStatus = "REVIEW_REQUIRED";
        }

        summary.setPortfolioId(req.getPortfolioId());
        summary.setValuationDate(req.getValuationDate());
        summary.setRequestId(req.getRequestId());
        summary.setGroupContributions(contributions);
        summary.setTotalContributionPct(totalContribution.setScale(6, RoundingMode.HALF_UP));
        summary.setStatus(overallStatus);
        summary.setDegraded(!warnings.isEmpty());
        summary.setWarnings(warnings.isEmpty() ? Collections.emptyList() : warnings);
        summary.setProcessedAt(Instant.now().toString());
        return summary;
    }
}
