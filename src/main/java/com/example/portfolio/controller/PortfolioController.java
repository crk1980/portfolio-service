package com.example.portfolio.controller;

import com.example.portfolio.dto.DailyReturnSummary;
import com.example.portfolio.dto.PortfolioValuationRequest;
import com.example.portfolio.service.PortfolioPerformanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/performance")
public class PortfolioController {
    private final PortfolioPerformanceService service;

    public PortfolioController(PortfolioPerformanceService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Portfolio Performance"));
    }

    @PostMapping("/daily-return")
    public ResponseEntity<DailyReturnSummary> dailyReturn(@Valid @RequestBody PortfolioValuationRequest req) {
        DailyReturnSummary summary = service.calculateDailyReturn(req);
        return ResponseEntity.ok(summary);
    }
}
