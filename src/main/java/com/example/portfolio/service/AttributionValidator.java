package com.example.portfolio.service;

import com.example.portfolio.dto.AttributionGroup;
import com.example.portfolio.dto.PortfolioAttributionRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class AttributionValidator {
    public List<String> validate(PortfolioAttributionRequest req) {
        List<String> errors = new ArrayList<>();

        if (req == null) {
            errors.add("Request cannot be null");
            return errors;
        }

        if (req.getPortfolioId() == null || req.getPortfolioId().isBlank()) {
            errors.add("portfolioId is required");
        }

        if (req.getRequestId() == null || req.getRequestId().isBlank()) {
            errors.add("requestId is required");
        }

        if (req.getCurrency() == null || req.getCurrency().isBlank()) {
            errors.add("currency is required");
        }

        if (req.getRequestedBy() == null || req.getRequestedBy().isBlank()) {
            errors.add("requestedBy is required");
        }

        if (req.getValuationDate() == null) {
            errors.add("valuationDate is required");
        }

        BigDecimal totalWeight = BigDecimal.ZERO;
        if (req.getGroups() == null || req.getGroups().isEmpty()) {
            errors.add("groups are required");
        } else {
            for (AttributionGroup group : req.getGroups()) {
                if (group.getGroupName() == null || group.getGroupName().isBlank()) {
                    errors.add("groupName is required for each group");
                }
                if (group.getWeightPct() == null) {
                    errors.add("weightPct is required for group " + group.getGroupName());
                } else if (group.getWeightPct().compareTo(BigDecimal.ZERO) < 0) {
                    errors.add("weightPct must be >= 0 for group " + group.getGroupName());
                } else {
                    totalWeight = totalWeight.add(group.getWeightPct());
                }
            }

            if (errors.isEmpty()) {
                if (totalWeight.compareTo(new BigDecimal("99")) < 0 || totalWeight.compareTo(new BigDecimal("101")) > 0) {
                    errors.add("Total group weight must be between 99 and 101 percent");
                }
            }
        }

        return errors;
    }
}
