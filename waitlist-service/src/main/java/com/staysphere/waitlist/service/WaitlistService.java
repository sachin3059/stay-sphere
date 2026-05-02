package com.staysphere.waitlist.service;

import com.staysphere.waitlist.dto.WaitlistRequest;
import com.staysphere.waitlist.dto.WaitlistResponse;
import com.staysphere.waitlist.entity.WaitlistEntry;
import com.staysphere.waitlist.exception.WaitlistException;
import com.staysphere.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final int SLOT_TTL_MINUTES = 30;

    @Transactional
    public WaitlistResponse joinWaitlist(WaitlistRequest request,
                                         String guestId) {
        // Prevent duplicate entries
        boolean alreadyWaiting = waitlistRepository
                .existsActiveEntry(
                        request.getPropertyId(), guestId,
                        request.getCheckIn(), request.getCheckOut(),
                        List.of(WaitlistEntry.WaitlistStatus.WAITING,
                                WaitlistEntry.WaitlistStatus.OFFERED));

        if (alreadyWaiting) {
            throw new WaitlistException(
                    "You are already on the waitlist for these dates.");
        }

        int position = waitlistRepository.countWaiting(
                request.getPropertyId(),
                request.getCheckIn(),
                request.getCheckOut()) + 1;

        WaitlistEntry entry = WaitlistEntry.builder()
                .propertyId(request.getPropertyId())
                .guestId(guestId)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .totalGuests(request.getTotalGuests())
                .queuePosition(position)
                .status(WaitlistEntry.WaitlistStatus.WAITING)
                .build();

        WaitlistEntry saved = waitlistRepository.save(entry);
        log.info("Guest {} joined waitlist at position {} for property {}",
                guestId, position, request.getPropertyId());

        return mapToResponse(saved);
    }

    // Triggered when booking_cancelled Kafka event arrives
    @KafkaListener(topics = "booking-events",
            groupId = "waitlist-group")
    @Transactional
    public void onBookingEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");

        if (!"booking_cancelled".equals(eventType)) return;

        String propertyId = (String) event.get("propertyId");
        String checkInStr = (String) event.get("checkIn");
        String checkOutStr = (String) event.get("checkOut");

        if (propertyId == null || checkInStr == null) return;

        LocalDate checkIn = LocalDate.parse(checkInStr);
        LocalDate checkOut = LocalDate.parse(checkOutStr);

        log.info("Booking cancelled for property {} — checking waitlist",
                propertyId);

        offerSlotToNext(propertyId, checkIn, checkOut);
    }

    @Transactional
    public void offerSlotToNext(String propertyId,
                                LocalDate checkIn,
                                LocalDate checkOut) {
        waitlistRepository.findFirstInQueue(
                        propertyId, checkIn, checkOut)
                .ifPresent(entry -> {
                    entry.setStatus(WaitlistEntry.WaitlistStatus.OFFERED);
                    entry.setSlotOfferedAt(LocalDateTime.now());
                    entry.setSlotExpiresAt(
                            LocalDateTime.now().plusMinutes(SLOT_TTL_MINUTES));
                    waitlistRepository.save(entry);

                    // Publish event so notification service alerts the guest
                    kafkaTemplate.send("waitlist-events",
                            entry.getId(),
                            Map.of(
                                    "event", "slot_offered",
                                    "waitlistId", entry.getId(),
                                    "guestId", entry.getGuestId(),
                                    "propertyId", entry.getPropertyId(),
                                    "checkIn", entry.getCheckIn().toString(),
                                    "checkOut", entry.getCheckOut().toString(),
                                    "expiresAt", entry.getSlotExpiresAt().toString()
                            ));

                    log.info("Slot offered to guest {} — expires at {}",
                            entry.getGuestId(), entry.getSlotExpiresAt());
                });
    }

    // Runs every 5 minutes — expire stale OFFERED entries
    // and offer slot to the next person in queue
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expireStaleOffers() {
        List<WaitlistEntry> expired = waitlistRepository
                .findExpiredOffers(LocalDateTime.now());

        for (WaitlistEntry entry : expired) {
            entry.setStatus(WaitlistEntry.WaitlistStatus.EXPIRED);
            waitlistRepository.save(entry);

            log.info("Waitlist slot expired for guest {} on property {}",
                    entry.getGuestId(), entry.getPropertyId());

            // Offer to next person in queue
            offerSlotToNext(entry.getPropertyId(),
                    entry.getCheckIn(), entry.getCheckOut());
        }
    }

    @Transactional
    public WaitlistResponse cancelWaitlist(String entryId, String guestId) {
        WaitlistEntry entry = waitlistRepository.findById(entryId)
                .orElseThrow(() ->
                        new WaitlistException("Waitlist entry not found"));

        if (!entry.getGuestId().equals(guestId)) {
            throw new WaitlistException(
                    "Not authorized to cancel this waitlist entry");
        }

        entry.setStatus(WaitlistEntry.WaitlistStatus.CANCELLED);
        return mapToResponse(waitlistRepository.save(entry));
    }

    public List<WaitlistResponse> getMyWaitlist(String guestId) {
        return waitlistRepository.findByGuestId(guestId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<WaitlistResponse> getPropertyWaitlist(
            String propertyId, LocalDate checkIn, LocalDate checkOut) {
        return waitlistRepository.findWaitingByPropertyAndDates(
                        propertyId, checkIn, checkOut)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private WaitlistResponse mapToResponse(WaitlistEntry w) {
        return WaitlistResponse.builder()
                .id(w.getId())
                .propertyId(w.getPropertyId())
                .guestId(w.getGuestId())
                .checkIn(w.getCheckIn())
                .checkOut(w.getCheckOut())
                .totalGuests(w.getTotalGuests())
                .queuePosition(w.getQueuePosition())
                .status(w.getStatus().name())
                .slotOfferedAt(w.getSlotOfferedAt())
                .slotExpiresAt(w.getSlotExpiresAt())
                .createdAt(w.getCreatedAt())
                .build();
    }
}