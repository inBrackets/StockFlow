package com.stockflow.marketservice.service;

import com.stockflow.marketservice.model.Stock;
import com.stockflow.marketservice.repository.StockRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceTickService {

    private final StockRepository stockRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final Map<String, BigDecimal> basePrices = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> lastTicks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService tickExecutor = Executors.newSingleThreadScheduledExecutor();

    @Scheduled(fixedRate = 30000, initialDelay = 1000)
    public void refreshBasePrices() {
        List<Stock> stocks = stockRepository.findAll();
        for (Stock stock : stocks) {
            basePrices.put(stock.getSymbol(), stock.getCurrentPrice());
            lastTicks.putIfAbsent(stock.getSymbol(), stock.getCurrentPrice());
        }
    }

    @PostConstruct
    public void startTickGeneration() {
        scheduleNextTick();
    }

    private void scheduleNextTick() {
        long delay = ThreadLocalRandom.current().nextLong(1000, 3001);
        tickExecutor.schedule(() -> {
            try {
                generateTick();
            } finally {
                scheduleNextTick();
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private void generateTick() {
        if (basePrices.isEmpty()) return;

        for (Map.Entry<String, BigDecimal> entry : basePrices.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal basePrice = entry.getValue();

            double changePercent = (ThreadLocalRandom.current().nextDouble() - 0.5) * 0.04;
            BigDecimal tickPrice = basePrice
                    .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(changePercent)))
                    .setScale(2, RoundingMode.HALF_UP);

            lastTicks.put(symbol, tickPrice);

            Map<String, Object> event = Map.of(
                    "symbol", symbol,
                    "price", tickPrice
            );
            kafkaTemplate.send("market.price.tick", symbol, event);
        }
        log.debug("Generated price ticks for {} stocks", basePrices.size());
    }

    public Map<String, BigDecimal> getLastTicks() {
        return Map.copyOf(lastTicks);
    }
}
