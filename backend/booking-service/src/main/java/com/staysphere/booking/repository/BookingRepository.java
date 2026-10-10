package com.staysphere.booking.repository;

import com.staysphere.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    List<Booking> findByGuestId(String guestId);

    List<Booking> findByPropertyId(String propertyId);

    Optional<Booking> findByIdempotencyKey(String idempotencyKey);

    @Query("SELECT b FROM Booking b WHERE b.propertyId = :propertyId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND b.checkIn < :checkOut AND b.checkOut > :checkIn")
    List<Booking> findConflictingBookings(
            @Param("propertyId") String propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' "
            + "AND b.pendingExpiresAt IS NOT NULL AND b.pendingExpiresAt < :now")
    List<Booking> findExpiredPending(@Param("now") LocalDateTime now);
}