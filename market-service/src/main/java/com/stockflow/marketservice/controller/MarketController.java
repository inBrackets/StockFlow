package com.stockflow.marketservice.controller;

import com.stockflow.marketservice.dto.PriceHistoryResponse;
import com.stockflow.marketservice.dto.StockResponse;
import com.stockflow.marketservice.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {
    private final MarketService marketService;

    @GetMapping("/stocks")
    public ResponseEntity<List<StockResponse>> getAllStocks() {
        return ResponseEntity.ok(marketService.getAllStocks());
    }

    @GetMapping("/stocks/{symbol}")
    public ResponseEntity<StockResponse> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(marketService.getStockBySymbol(symbol));
    }

    @GetMapping("/stocks/{symbol}/history")
    public ResponseEntity<List<PriceHistoryResponse>> getPriceHistory(@PathVariable String symbol) {
        return ResponseEntity.ok(marketService.getPriceHistory(symbol));
    }
}
