package com.stockflow.marketservice.controller;

import com.stockflow.marketservice.service.PriceTickService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/market")
@RequiredArgsConstructor
public class PriceStreamController {

    private final PriceTickService priceTickService;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    @GetMapping("/stocks/stream")
    public SseEmitter streamPrices() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));

        // Send current ticks immediately
        try {
            Map<String, BigDecimal> ticks = priceTickService.getLastTicks();
            for (Map.Entry<String, BigDecimal> entry : ticks.entrySet()) {
                emitter.send(SseEmitter.event()
                        .name("price-tick")
                        .data(Map.of("symbol", entry.getKey(), "price", entry.getValue())));
            }
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    @KafkaListener(topics = "market.price.tick", groupId = "market-sse-stream")
    public void onPriceTick(Map<String, Object> event) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("price-tick")
                        .data(event));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }
}
