package com.staysphere.availability.service;

import com.staysphere.availability.dto.*;
import com.staysphere.availability.entity.BlockedDate;
import com.staysphere.availability.repository.BlockedDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final BlockedDateRepository blockedDateRepository;
    private final StringRedisTemplate redisTemplate;

    // ── Kafka consumer ──────────────────────────────────────────

    @KafkaListener(topics = "booking-events",
            groupId = "availability-group")
    @Transactional
    public void onBookingEvent(Map<String, Object> event) {
        String eventType = (String) event.get("event");

        switch (eventType) {
            case "booking_confirmed" -> blockDatesOnConfirm(event);
            case "booking_cancelled" -> unblockDatesOnCancel(event);
        }
    }

    private void blockDatesOnConfirm(Map<String, Object> event) {
        String propertyId = (String) event.get("propertyId");
        String bookingId  = (String) event.get("bookingId");
        String checkIn    = (String) event.get("checkIn");
        String checkOut   = (String) event.get("checkOut");

        if (propertyId == null || checkIn == null) return;

        BlockedDate blocked = BlockedDate.builder()
                .propertyId(propertyId)
                .startDate(LocalDate.parse(checkIn))
                .endDate(LocalDate.parse(checkOut))
                .reason(BlockedDate.BlockReason.BOOKED)
                .referenceId(bookingId)
                .build();

        blockedDateRepository.save(blocked);
        invalidateCache(propertyId);
        log.info("Dates blocked for property {} — booking {}",
                propertyId, bookingId);
    }

    private void unblockDatesOnCancel(Map<String, Object> event) {
        String propertyId = (String) event.get("propertyId");
        String bookingId  = (String) event.get("bookingId");

        if (bookingId == null) return;

        blockedDateRepository.deleteByReferenceId(bookingId);
        invalidateCache(propertyId);
        log.info("Dates unblocked for property {} — booking cancelled {}",
                propertyId, bookingId);
    }

    // ── Core interval merge algorithm ───────────────────────────

    public List<AvailableRangeResponse> getAvailableRanges(
            String propertyId, LocalDate from, LocalDate to) {

        // Try cache first
        String cacheKey = "availability:" + propertyId + ":" + from + ":" + to;
        // (cache logic simplified — full Redis caching in production)

        List<BlockedDate> blocked = blockedDateRepository
                .findBlockedInRange(propertyId, from, to);

        List<AvailableRangeResponse> available =
                mergeAndFindGaps(blocked, from, to);

        log.info("Property {} has {} available ranges between {} and {}",
                propertyId, available.size(), from, to);

        return available;
    }

    // The interval merge algorithm
    // Input:  list of [blocked start, blocked end] intervals
    // Output: list of [available start, available end] gaps between them
    private List<AvailableRangeResponse> mergeAndFindGaps(
            List<BlockedDate> blockedDates,
            LocalDate rangeStart,
            LocalDate rangeEnd) {

        if (blockedDates.isEmpty()) {
            // Nothing blocked — entire range is available
            return List.of(AvailableRangeResponse.builder()
                    .from(rangeStart)
                    .to(rangeEnd)
                    .nights(ChronoUnit.DAYS.between(rangeStart, rangeEnd))
                    .build());
        }

        // Step 1 — Sort intervals by start date
        List<BlockedDate> sorted = blockedDates.stream()
                .sorted(Comparator.comparing(BlockedDate::getStartDate))
                .collect(Collectors.toList());

        // Step 2 — Merge overlapping intervals
        List<LocalDate[]> merged = new ArrayList<>();
        LocalDate[] current = {
                sorted.get(0).getStartDate(),
                sorted.get(0).getEndDate()
        };

        for (int i = 1; i < sorted.size(); i++) {
            BlockedDate next = sorted.get(i);
            if (!next.getStartDate().isAfter(current[1])) {
                // Overlapping — extend current interval if needed
                if (next.getEndDate().isAfter(current[1])) {
                    current[1] = next.getEndDate();
                }
            } else {
                // No overlap — push current, start new
                merged.add(current);
                current = new LocalDate[]{
                        next.getStartDate(), next.getEndDate()
                };
            }
        }
        merged.add(current);

        // Step 3 — Find gaps between merged blocked intervals
        List<AvailableRangeResponse> available = new ArrayList<>();
        LocalDate pointer = rangeStart;

        for (LocalDate[] interval : merged) {
            LocalDate blockStart = interval[0];
            LocalDate blockEnd   = interval[1];

            // Clamp to query range
            if (blockStart.isAfter(rangeEnd)) break;
            if (blockEnd.isBefore(rangeStart)) continue;

            // Gap before this blocked interval
            if (pointer.isBefore(blockStart)) {
                available.add(AvailableRangeResponse.builder()
                        .from(pointer)
                        .to(blockStart)
                        .nights(ChronoUnit.DAYS.between(pointer, blockStart))
                        .build());
            }

            // Move pointer past this blocked interval
            if (blockEnd.isAfter(pointer)) {
                pointer = blockEnd;
            }
        }

        // Gap after last blocked interval
        if (pointer.isBefore(rangeEnd)) {
            available.add(AvailableRangeResponse.builder()
                    .from(pointer)
                    .to(rangeEnd)
                    .nights(ChronoUnit.DAYS.between(pointer, rangeEnd))
                    .build());
        }

        return available;
    }

    // ── Check specific dates ─────────────────────────────────────

    public AvailabilityCheckResponse checkAvailability(
            String propertyId,
            LocalDate checkIn,
            LocalDate checkOut) {

        boolean blocked = blockedDateRepository
                .isDateRangeBlocked(propertyId, checkIn, checkOut);

        return AvailabilityCheckResponse.builder()
                .propertyId(propertyId)
                .checkIn(checkIn)
                .checkOut(checkOut)
                .available(!blocked)
                .message(blocked
                        ? "Property is not available for selected dates"
                        : "Property is available for selected dates")
                .build();
    }

    // ── Host manually blocks dates ────────────────────────────────

    @Transactional
    public BlockedDateResponse blockDates(BlockedDateRequest request) {
        BlockedDate blocked = BlockedDate.builder()
                .propertyId(request.getPropertyId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(BlockedDate.BlockReason.valueOf(
                        request.getReason().toUpperCase()))
                .build();

        BlockedDate saved = blockedDateRepository.save(blocked);
        invalidateCache(request.getPropertyId());
        return mapToResponse(saved);
    }

    public List<BlockedDateResponse> getBlockedDates(String propertyId) {
        return blockedDateRepository
                .findByPropertyIdOrderByStartDateAsc(propertyId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void invalidateCache(String propertyId) {
        // Delete all Redis keys matching this property
        Set<String> keys = redisTemplate.keys(
                "availability:" + propertyId + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private BlockedDateResponse mapToResponse(BlockedDate b) {
        return BlockedDateResponse.builder()
                .id(b.getId())
                .propertyId(b.getPropertyId())
                .startDate(b.getStartDate())
                .endDate(b.getEndDate())
                .reason(b.getReason().name())
                .referenceId(b.getReferenceId())
                .createdAt(b.getCreatedAt())
                .build();
    }
}