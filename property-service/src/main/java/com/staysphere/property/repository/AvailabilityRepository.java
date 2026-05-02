package com.staysphere.property.repository;

import com.staysphere.property.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, String> {

    @Query("SELECT a FROM Availability a WHERE a.property.id = :propertyId " +
            "AND a.startDate <= :endDate AND a.endDate >= :startDate")
    List<Availability> findConflicts(
            @Param("propertyId") String propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Availability> findByPropertyId(String propertyId);
}