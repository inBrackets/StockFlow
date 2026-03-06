package com.stockflow.portfolioservice.kafka;

import com.stockflow.portfolioservice.model.Portfolio;
import com.stockflow.portfolioservice.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioEventConsumer {
    private final PortfolioRepository portfolioRepository;

    @KafkaListener(topics = "user.registered", groupId = "portfolio-service")
    public void onUserRegistered(Map<String, Object> event) {
        Long userId = ((Number) event.get("userId")).longValue();
        log.info("Received user.registered event for userId={}", userId);

        if (!portfolioRepository.existsByUserId(userId)) {
            Portfolio portfolio = Portfolio.builder()
                    .userId(userId)
                    .totalValue(BigDecimal.ZERO)
                    .build();
            portfolioRepository.save(portfolio);
            log.info("Auto-created portfolio for userId={}", userId);
        }
    }

    @KafkaListener(topics = "market.price.updated", groupId = "portfolio-service")
    public void onMarketPriceUpdated(Map<String, Object> event) {
        log.info("Received market.price.updated event: {}", event);
    }
}
