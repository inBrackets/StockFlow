package com.stockflow.userservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendUserRegistered(Long userId, String username) {
        Map<String, Object> event = Map.of(
            "userId", userId,
            "username", username
        );
        kafkaTemplate.send("user.registered", String.valueOf(userId), event);
        log.info("Sent user.registered event for userId={}", userId);
    }
}
