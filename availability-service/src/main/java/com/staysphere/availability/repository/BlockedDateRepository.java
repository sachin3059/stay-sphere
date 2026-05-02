package com.staysphere.availability.repository;

import com.staysphere.availability.entity.BlockedDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BlockedDateRepository
        extends JpaRepository<BlockedDate, String> {

    // All blocked intervals for a property within a date range
    @Query("SELECT b FROM BlockedDate b WHERE b.propertyId = :propertyId " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
            "ORDER BY b.startDate ASC")
    List<BlockedDate> findBlockedInRange(
            @Param("propertyId") String propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // All blocked dates for a property ever
    List<BlockedDate> findByPropertyIdOrderByStartDateAsc(
            String propertyId);

    // Check if specific dates are blocked
    @Query("SELECT COUNT(b) > 0 FROM BlockedDate b " +
            "WHERE b.propertyId = :propertyId " +
            "AND b.startDate <= :endDate AND b.endDate >= :startDate")
    boolean isDateRangeBlocked(
            @Param("propertyId") String propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    void deleteByReferenceId(String referenceId);
}