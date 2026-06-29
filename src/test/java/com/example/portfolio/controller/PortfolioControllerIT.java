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
                .andExpect(jsonPath("$.groupContributions[0].pricingMode").value("FALLBACK"))
                .andExpect(jsonPath("$.degraded").value(true))
                .andExpect(jsonPath("$.warnings[0]").value("Fall back pricing used for Cash"));
    }
}
