package com.stockflow.portfolioservice.service;

import com.stockflow.portfolioservice.dto.*;
import com.stockflow.portfolioservice.kafka.PortfolioEventProducer;
import com.stockflow.portfolioservice.model.Portfolio;
import com.stockflow.portfolioservice.model.Trade;
import com.stockflow.portfolioservice.model.TradeType;
import com.stockflow.portfolioservice.repository.PortfolioRepository;
import com.stockflow.portfolioservice.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final TradeRepository tradeRepository;
    private final PortfolioEventProducer eventProducer;

    public PortfolioResponse getPortfolio(Long userId) {
        Portfolio portfolio = portfolioRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found for userId=" + userId));

        List<Trade> trades = tradeRepository.findByPortfolioIdOrderByTradedAtDesc(portfolio.getId());

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .userId(portfolio.getUserId())
                .totalValue(portfolio.getTotalValue())
                .trades(trades.stream().map(this::toTradeResponse).collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public TradeResponse executeBuy(TradeRequest request) {
        return executeTrade(request, TradeType.BUY);
    }

    @Transactional
    public TradeResponse executeSell(TradeRequest request) {
        return executeTrade(request, TradeType.SELL);
    }

    private TradeResponse executeTrade(TradeRequest request, TradeType tradeType) {
        Portfolio portfolio = portfolioRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        Trade trade = Trade.builder()
                .portfolio(portfolio)
                .stockSymbol(request.getStockSymbol())
                .quantity(request.getQuantity())
                .priceAtTrade(request.getPrice())
                .tradeType(tradeType)
                .build();

        trade = tradeRepository.save(trade);
        log.info("Executed {} trade: {} x {} @ {}", tradeType, request.getStockSymbol(), request.getQuantity(), request.getPrice());

        recalculateTotalValue(portfolio);

        eventProducer.sendTradeExecuted(
                request.getStockSymbol(),
                request.getQuantity(),
                request.getPrice(),
                tradeType.name()
        );

        return toTradeResponse(trade);
    }

    private void recalculateTotalValue(Portfolio portfolio) {
        List<Trade> allTrades = tradeRepository.findByPortfolioIdOrderByTradedAtDesc(portfolio.getId());
        Map<String, Integer> holdings = new HashMap<>();
        Map<String, BigDecimal> lastPrice = new HashMap<>();

        for (Trade t : allTrades) {
            int current = holdings.getOrDefault(t.getStockSymbol(), 0);
            if (t.getTradeType() == TradeType.BUY) {
                holdings.put(t.getStockSymbol(), current + t.getQuantity());
            } else {
                holdings.put(t.getStockSymbol(), current - t.getQuantity());
            }
            lastPrice.putIfAbsent(t.getStockSymbol(), t.getPriceAtTrade());
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
            if (entry.getValue() > 0) {
                BigDecimal price = lastPrice.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                total = total.add(price.multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }

        portfolio.setTotalValue(total);
        portfolioRepository.save(portfolio);
    }

    private TradeResponse toTradeResponse(Trade trade) {
        return TradeResponse.builder()
                .id(trade.getId())
                .stockSymbol(trade.getStockSymbol())
                .quantity(trade.getQuantity())
                .priceAtTrade(trade.getPriceAtTrade())
                .tradeType(trade.getTradeType().name())
                .tradedAt(trade.getTradedAt())
                .build();
    }
}
