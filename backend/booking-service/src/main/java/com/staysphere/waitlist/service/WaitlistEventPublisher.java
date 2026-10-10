package com.staysphere.waitlist.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staysphere.booking.outbox.OutboxEvent;
import com.staysphere.booking.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WaitlistEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public void publishSlotOffered(String waitlistId, Map<String, Object> payload) {
        try {
            payload.putIfAbsent("eventId", waitlistId + ":slot_offered");
            outboxEventRepository.save(OutboxEvent.builder()
                    .topic("waitlist-events")
                    .messageKey(waitlistId)
                    .payload(objectMapper.writeValueAsString(payload))
                    .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue waitlist event", e);
        }
    }
}
