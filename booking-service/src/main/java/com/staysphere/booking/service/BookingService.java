package com.staysphere.booking.service;

import com.staysphere.booking.dto.BookingRequest;
import com.staysphere.booking.dto.BookingResponse;
import com.staysphere.booking.entity.Booking;
import com.staysphere.booking.exception.BookingException;
import com.staysphere.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisLockService redisLockService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public BookingResponse createBooking(BookingRequest request,
                                         String guestId) {
        validateDates(request.getCheckIn(), request.getCheckOut());

        String lockKey = redisLockService.buildLockKey(
                request.getPropertyId(),
                request.getCheckIn().toString(),
                request.getCheckOut().toString());

        if (!redisLockService.acquireLock(lockKey)) {
            throw new BookingException(
                    "Property is being booked by another user. Try again shortly.");
        }

        try {
            List<Booking> conflicts = bookingRepository
                    .findConflictingBookings(
                            request.getPropertyId(),
                            request.getCheckIn(),
                            request.getCheckOut());

            if (!conflicts.isEmpty()) {
                throw new BookingException(
                        "Property is not available for selected dates.");
            }

            long nights = ChronoUnit.DAYS.between(
                    request.getCheckIn(), request.getCheckOut());

            BigDecimal totalPrice = request.getPricePerNight()
                    .multiply(BigDecimal.valueOf(nights));

            String idempotencyKey = UUID.randomUUID().toString();

            Booking booking = Booking.builder()
                    .propertyId(request.getPropertyId())
                    .guestId(guestId)
                    .hostId(request.getHostId())
                    .checkIn(request.getCheckIn())
                    .checkOut(request.getCheckOut())
                    .totalGuests(request.getTotalGuests())
                    .pricePerNight(request.getPricePerNight())
                    .totalPrice(totalPrice)
                    .status(Booking.BookingStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .build();

            Booking saved = bookingRepository.save(booking);

            kafkaTemplate.send("booking-events",
                    saved.getId(),
                    Map.of(
                            "event", "booking_created",
                            "bookingId", saved.getId(),
                            "propertyId", saved.getPropertyId(),
                            "guestId", saved.getGuestId(),
                            "hostId", saved.getHostId(),
                            "checkIn", saved.getCheckIn().toString(),
                            "checkOut", saved.getCheckOut().toString(),
                            "totalPrice", saved.getTotalPrice().toString(),
                            "idempotencyKey", idempotencyKey
                    ));

            log.info("Booking created: {}", saved.getId());
            return mapToResponse(saved);

        } catch (BookingException e) {
            redisLockService.releaseLock(lockKey);
            throw e;
        }
    }

    @Transactional
    public BookingResponse confirmBooking(String bookingId) {
        Booking booking = findBookingById(bookingId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException("Booking is not in PENDING state.");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        String lockKey = redisLockService.buildLockKey(
                booking.getPropertyId(),
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString());
        redisLockService.releaseLock(lockKey);

        kafkaTemplate.send("booking-events",
                saved.getId(),
                Map.of(
                        "event", "booking_confirmed",
                        "bookingId", saved.getId(),
                        "propertyId", saved.getPropertyId(),
                        "guestId", saved.getGuestId(),
                        "hostId", saved.getHostId(),
                        "checkIn", saved.getCheckIn().toString(),
                        "checkOut", saved.getCheckOut().toString()
                ));

        log.info("Booking confirmed: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingId, String userId) {
        Booking booking = findBookingById(bookingId);

        if (!booking.getGuestId().equals(userId) &&
                !booking.getHostId().equals(userId)) {
            throw new BookingException(
                    "You are not authorized to cancel this booking.");
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled.");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);

        String lockKey = redisLockService.buildLockKey(
                booking.getPropertyId(),
                booking.getCheckIn().toString(),
                booking.getCheckOut().toString());
        redisLockService.releaseLock(lockKey);

        kafkaTemplate.send("booking-events",
                saved.getId(),
                Map.of(
                        "event", "booking_cancelled",
                        "bookingId", saved.getId(),
                        "propertyId", saved.getPropertyId(),
                        "guestId", saved.getGuestId(),
                        "checkIn", saved.getCheckIn().toString(),
                        "checkOut", saved.getCheckOut().toString()
                ));

        log.info("Booking cancelled: {}", saved.getId());
        return mapToResponse(saved);
    }

    public BookingResponse getBooking(String bookingId) {
        return mapToResponse(findBookingById(bookingId));
    }

    public List<BookingResponse> getGuestBookings(String guestId) {
        return bookingRepository.findByGuestId(guestId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getPropertyBookings(String propertyId) {
        return bookingRepository.findByPropertyId(propertyId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found: " + bookingId));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new BookingException("Check-in and check-out dates required.");
        }
        if (!checkIn.isAfter(LocalDate.now())) {
            throw new BookingException("Check-in must be a future date.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BookingException(
                    "Check-out must be after check-in.");
        }
    }

    private BookingResponse mapToResponse(Booking b) {
        return BookingResponse.builder()
                .id(b.getId())
                .propertyId(b.getPropertyId())
                .guestId(b.getGuestId())
                .hostId(b.getHostId())
                .checkIn(b.getCheckIn())
                .checkOut(b.getCheckOut())
                .totalGuests(b.getTotalGuests())
                .totalPrice(b.getTotalPrice())
                .pricePerNight(b.getPricePerNight())
                .status(b.getStatus().name())
                .idempotencyKey(b.getIdempotencyKey())
                .createdAt(b.getCreatedAt())
                .confirmedAt(b.getConfirmedAt())
                .build();
    }
}