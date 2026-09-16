package com.staysphere.booking.outbox;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:2000}")
    @Transactional
    public void publishPending() {
        for (OutboxEvent event : outboxEventRepository.findUnpublishedForUpdate()) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        event.getPayload(), new TypeReference<>() {});
                kafkaTemplate.send(event.getTopic(), event.getMessageKey(), payload);
                event.setPublishedAt(Instant.now());
                outboxEventRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
            }
        }
    }
}
