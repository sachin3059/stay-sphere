package com.staysphere.booking.service;

import com.staysphere.booking.client.PropertyIntegrationClient;
import com.staysphere.booking.dto.BookingRequest;
import com.staysphere.booking.dto.BookingResponse;
import com.staysphere.booking.entity.Booking;
import com.staysphere.booking.exception.BookingException;
import com.staysphere.booking.repository.BookingRepository;
import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RedisLockService redisLockService;
    private final PropertyIntegrationClient propertyClient;
    private final BookingEventPublisher eventPublisher;

    @Value("${booking.pending-ttl-minutes:30}")
    private long pendingTtlMinutes;

    @Transactional
    public BookingResponse createBooking(BookingRequest request,
                                         String guestId,
                                         String idempotencyKey) {
        String key = (idempotencyKey == null || idempotencyKey.isBlank())
                ? UUID.randomUUID().toString()
                : idempotencyKey;
        return bookingRepository.findByIdempotencyKey(key)
                .map(this::mapToResponse)
                .orElseGet(() -> doCreateBooking(request, guestId, key));
    }

    private BookingResponse doCreateBooking(BookingRequest request,
                                            String guestId,
                                            String idempotencyKey) {
        validateDates(request.getCheckIn(), request.getCheckOut());

        if (!propertyClient.isRangeAvailable(
                request.getPropertyId(), request.getCheckIn(), request.getCheckOut())) {
            throw new BookingException("Property is not available for selected dates.");
        }

        RedisLockService.LockHandle lock =
                redisLockService.acquirePropertyLock(request.getPropertyId());
        if (!lock.acquired()) {
            boolean ownPending = bookingRepository.findByGuestId(guestId).stream()
                    .anyMatch(b -> b.getPropertyId().equals(request.getPropertyId())
                            && b.getStatus() == Booking.BookingStatus.PENDING);
            if (ownPending) {
                throw new BookingException(
                        "You already have a pending booking for this property. "
                                + "Open My trips to pay or cancel it, then try again.");
            }
            throw new BookingException(
                    "Property is being booked by another user. Try again shortly.");
        }

        try {
            if (!bookingRepository.findConflictingBookings(
                    request.getPropertyId(),
                    request.getCheckIn(),
                    request.getCheckOut()).isEmpty()) {
                throw new BookingException(
                        "Property is not available for selected dates.");
            }

            PropertyIntegrationClient.PriceQuote quote = propertyClient.calculatePrice(
                    request.getPropertyId(), request.getCheckIn(), request.getCheckOut());

            Booking booking = Booking.builder()
                    .propertyId(request.getPropertyId())
                    .guestId(guestId)
                    .hostId(request.getHostId())
                    .checkIn(request.getCheckIn())
                    .checkOut(request.getCheckOut())
                    .totalGuests(request.getTotalGuests())
                    .pricePerNight(quote.pricePerNight())
                    .totalPrice(quote.totalPrice())
                    .status(Booking.BookingStatus.PENDING)
                    .idempotencyKey(idempotencyKey)
                    .propertyLockKey(lock.lockKey())
                    .propertyLockToken(lock.token())
                    .pendingExpiresAt(LocalDateTime.now().plusMinutes(pendingTtlMinutes))
                    .build();

            Booking saved;
            try {
                saved = bookingRepository.save(booking);
            } catch (DataIntegrityViolationException ex) {
                return bookingRepository.findByIdempotencyKey(idempotencyKey)
                        .map(this::mapToResponse)
                        .orElseThrow(() -> new BookingException(
                                "Booking conflict for selected dates."));
            }

            eventPublisher.publishBookingCreated(saved);
            log.info("Booking created: {}", saved.getId());
            return mapToResponse(saved);
        } catch (BookingException e) {
            throw e;
        } finally {
            // Hold lock only for the create transaction; overlaps are enforced in Postgres.
            redisLockService.releaseLock(lock);
        }
    }

    @Transactional
    public BookingResponse confirmBooking(String bookingId, String userId) {
        Booking booking = findBookingById(bookingId);
        assertGuestOrHost(booking, userId);
        return confirmBookingInternal(bookingId);
    }

    @Transactional
    public BookingResponse confirmBookingInternal(String bookingId) {
        Booking booking = findBookingById(bookingId);
        if (booking.getStatus() == Booking.BookingStatus.CONFIRMED) {
            return mapToResponse(booking);
        }
        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BookingException("Booking is not in PENDING state.");
        }
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        releasePropertyLock(booking);
        eventPublisher.publishBookingConfirmed(saved);
        return mapToResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(String bookingId, String userId) {
        Booking booking = findBookingById(bookingId);
        assertGuestOrHost(booking, userId);
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BookingException("Booking is already cancelled.");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        releasePropertyLock(booking);
        eventPublisher.publishBookingCancelled(saved);
        return mapToResponse(saved);
    }

    public BookingResponse getBooking(String bookingId, String userId) {
        Booking booking = findBookingById(bookingId);
        assertGuestOrHost(booking, userId);
        return mapToResponse(booking);
    }

    public List<BookingResponse> getGuestBookings(String guestId) {
        return bookingRepository.findByGuestId(guestId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<BookingResponse> getPropertyBookings(String propertyId, String hostId) {
        return bookingRepository.findByPropertyId(propertyId)
                .stream()
                .filter(b -> b.getHostId().equals(hostId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void expirePendingBookings() {
        for (Booking booking : bookingRepository.findExpiredPending(LocalDateTime.now())) {
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            releasePropertyLock(booking);
            eventPublisher.publishBookingCancelled(booking);
            log.info("Booking expired: {}", booking.getId());
        }
    }

    private void releasePropertyLock(Booking booking) {
        if (booking.getPropertyLockKey() != null && booking.getPropertyLockToken() != null) {
            redisLockService.releaseLock(new RedisLockService.LockHandle(
                    booking.getPropertyLockKey(),
                    booking.getPropertyLockToken(),
                    true));
        }
    }

    private void assertGuestOrHost(Booking booking, String userId) {
        if (!booking.getGuestId().equals(userId)
                && !booking.getHostId().equals(userId)) {
            throw new BadRequestException(
                    "You are not authorized to access this booking.");
        }
    }

    private Booking findBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking not found: " + bookingId));
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
