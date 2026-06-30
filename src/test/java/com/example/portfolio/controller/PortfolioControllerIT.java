package com.example.portfolio.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void attributionEndpointReturnsFallbackPricingAndWarnings() throws Exception {
        String requestBody = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12345\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"Cash\"," +
                "\"weightPct\":40" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.portfolioId").value("PORT1"))
                .andExpect(jsonPath("$.requestId").value("REQ-12345"))
                .andExpect(jsonPath("$.totalContributionPct").value("0.180000"))
                .andExpect(jsonPath("$.groupContributions[0].groupName").value("Cash"))
                .andExpect(jsonPath("$.groupContributions[0].pricingMode").value("FALLBACK_USED"))
                .andExpect(jsonPath("$.degraded").value(true))
                .andExpect(jsonPath("$.warnings[0]").value("Fallback return used for Cash"));
    }

    @Test
    void attributionEndpointValidPrimaryPricing() throws Exception {
        String requestBody = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12346\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"equity\"," +
                "\"weightPct\":60," +
                "\"returnPct\":1.5" +
                "},{" +
                "\"groupName\":\"fixed income\"," +
                "\"weightPct\":40," +
                "\"returnPct\":0.75" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VALID"))
                .andExpect(jsonPath("$.degraded").value(false))
                .andExpect(jsonPath("$.totalContributionPct").value("1.050000"));
    }

    @Test
    void attributionEndpointInvalidWeightRanges() throws Exception {
        String requestBody = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12347\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"equity\"," +
                "\"weightPct\":98," +
                "\"returnPct\":1.5" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INVALID_INPUT"));
    }

    @Test
    void attributionEndpointReviewRequiredWhenMultipleMissingReturns() throws Exception {
        String requestBody = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12348\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"real estate\"," +
                "\"weightPct\":50" +
                "},{" +
                "\"groupName\":\"commodities\"," +
                "\"weightPct\":50" +
                "}]" +
                "}";

        mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.degraded").value(true));
    }

    @Test
    void attributionEndpointIsIdempotentForSameRequestId() throws Exception {
        String requestBody1 = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12349\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"equity\"," +
                "\"weightPct\":60," +
                "\"returnPct\":1.5" +
                "}]" +
                "}";

        String requestBody2 = "{" +
                "\"portfolioId\":\"PORT1\"," +
                "\"valuationDate\":[2026,6,28]," +
                "\"requestId\":\"REQ-12349\"," +
                "\"currency\":\"USD\"," +
                "\"requestedBy\":\"advisor02\"," +
                "\"groups\":[{" +
                "\"groupName\":\"cash\"," +
                "\"weightPct\":40," +
                "\"returnPct\":0.5" +
                "}]" +
                "}";

        var firstResult = mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody1))
                .andExpect(status().isOk())
                .andReturn();

        mockMvc.perform(post("/api/performance/attribution")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value("REQ-12349"))
                .andExpect(jsonPath("$.totalContributionPct").value("0.900000"));
    }
}
