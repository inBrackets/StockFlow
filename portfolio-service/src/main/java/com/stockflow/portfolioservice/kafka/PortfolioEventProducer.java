package com.stockflow.portfolioservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendTradeExecuted(String stockSymbol, int quantity, BigDecimal price, String tradeType) {
        Map<String, Object> event = Map.of(
            "stockSymbol", stockSymbol,
            "quantity", quantity,
            "price", price,
            "tradeType", tradeType
        );
        kafkaTemplate.send("portfolio.trade.executed", stockSymbol, event);
        log.info("Sent portfolio.trade.executed event for symbol={}", stockSymbol);
    }
}
