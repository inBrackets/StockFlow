package com.stockflow.marketservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPriceUpdated(String symbol, BigDecimal price) {
        Map<String, Object> event = Map.of(
            "symbol", symbol,
            "price", price
        );
        kafkaTemplate.send("market.price.updated", symbol, event);
        log.info("Sent market.price.updated for symbol={} price={}", symbol, price);
    }
}
