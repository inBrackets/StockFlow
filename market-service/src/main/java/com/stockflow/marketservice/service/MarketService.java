package com.stockflow.marketservice.service;

import com.stockflow.marketservice.dto.PriceHistoryResponse;
import com.stockflow.marketservice.dto.StockResponse;
import com.stockflow.marketservice.kafka.MarketEventProducer;
import com.stockflow.marketservice.model.PriceHistory;
import com.stockflow.marketservice.model.Stock;
import com.stockflow.marketservice.repository.PriceHistoryRepository;
import com.stockflow.marketservice.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final MarketEventProducer eventProducer;
    private final Random random = new Random();

    public List<StockResponse> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toStockResponse)
                .collect(Collectors.toList());
    }

    public StockResponse getStockBySymbol(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        return toStockResponse(stock);
    }

    public List<PriceHistoryResponse> getPriceHistory(String symbol) {
        Stock stock = stockRepository.findBySymbol(symbol.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Stock not found: " + symbol));
        return priceHistoryRepository.findByStockIdOrderByRecordedAtDesc(stock.getId()).stream()
                .map(ph -> PriceHistoryResponse.builder()
                        .price(ph.getPrice())
                        .recordedAt(ph.getRecordedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void refreshPrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            double changePercent = (random.nextDouble() - 0.5) * 0.04;
            BigDecimal newPrice = stock.getCurrentPrice()
                    .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(changePercent)))
                    .setScale(2, RoundingMode.HALF_UP);

            stock.setCurrentPrice(newPrice);
            stockRepository.save(stock);

            PriceHistory history = PriceHistory.builder()
                    .stock(stock)
                    .price(newPrice)
                    .build();
            priceHistoryRepository.save(history);

            eventProducer.sendPriceUpdated(stock.getSymbol(), newPrice);
        }
        log.info("Refreshed prices for {} stocks", stocks.size());
    }

    private StockResponse toStockResponse(Stock stock) {
        return StockResponse.builder()
                .id(stock.getId())
                .symbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .build();
    }
}
