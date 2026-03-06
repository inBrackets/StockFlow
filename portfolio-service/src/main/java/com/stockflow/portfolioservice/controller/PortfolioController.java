package com.stockflow.portfolioservice.controller;

import com.stockflow.portfolioservice.dto.*;
import com.stockflow.portfolioservice.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping("/{userId}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(userId));
    }

    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buy(@Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(portfolioService.executeBuy(request));
    }

    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sell(@Valid @RequestBody TradeRequest request) {
        return ResponseEntity.ok(portfolioService.executeSell(request));
    }
}
