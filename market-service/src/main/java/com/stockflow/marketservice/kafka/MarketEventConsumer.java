package com.stockflow.marketservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class MarketEventConsumer {
    @KafkaListener(topics = "portfolio.trade.executed", groupId = "market-service")
    public void onTradeExecuted(Map<String, Object> event) {
        log.info("Received portfolio.trade.executed event: {}", event);
    }
}
