package com.staysphere.waitlist.repository;

import com.staysphere.waitlist.entity.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WaitlistRepository
        extends JpaRepository<WaitlistEntry, String> {

    // Get all waiting entries for a property+dates, ordered by position
    @Query("SELECT w FROM WaitlistEntry w WHERE w.propertyId = :propertyId " +
            "AND w.checkIn = :checkIn AND w.checkOut = :checkOut " +
            "AND w.status = 'WAITING' ORDER BY w.queuePosition ASC")
    List<WaitlistEntry> findWaitingByPropertyAndDates(
            @Param("propertyId") String propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    // Get next in queue (lowest position number)
    @Query("SELECT w FROM WaitlistEntry w WHERE w.propertyId = :propertyId " +
            "AND w.checkIn = :checkIn AND w.checkOut = :checkOut " +
            "AND w.status = 'WAITING' ORDER BY w.queuePosition ASC")
    Optional<WaitlistEntry> findFirstInQueue(
            @Param("propertyId") String propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    List<WaitlistEntry> findByGuestId(String guestId);

    // Find expired OFFERED entries for cleanup
    @Query("SELECT w FROM WaitlistEntry w WHERE w.status = 'OFFERED' " +
            "AND w.slotExpiresAt < :now")
    List<WaitlistEntry> findExpiredOffers(
            @Param("now") LocalDateTime now);

    // Count queue position
    @Query("SELECT COUNT(w) FROM WaitlistEntry w " +
            "WHERE w.propertyId = :propertyId " +
            "AND w.checkIn = :checkIn AND w.checkOut = :checkOut " +
            "AND w.status = 'WAITING'")
    int countWaiting(
            @Param("propertyId") String propertyId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);

    @Query("SELECT COUNT(w) > 0 FROM WaitlistEntry w WHERE w.propertyId = :propertyId " +
            "AND w.guestId = :guestId " +
            "AND w.checkIn = :checkIn " +
            "AND w.checkOut = :checkOut " +
            "AND w.status IN :statuses")
    boolean existsActiveEntry(
            @Param("propertyId") String propertyId,
            @Param("guestId") String guestId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("statuses") List<WaitlistEntry.WaitlistStatus> statuses);
}