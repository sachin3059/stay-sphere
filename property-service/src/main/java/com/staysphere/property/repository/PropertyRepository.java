package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, String> {

    List<Property> findByHostId(String hostId);

    @Query("SELECT p.id FROM Property p WHERE p.status = 'ACTIVE'")
    List<String> findAllActiveIds();

    List<Property> findByCityIgnoreCaseAndStatus(
            String city, Property.PropertyStatus status);

    @Query("SELECT p FROM Property p WHERE p.status = 'ACTIVE' " +
            "AND LOWER(p.city) = LOWER(:city) " +
            "AND p.maxGuests >= :guests " +
            "AND p.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<Property> searchProperties(
            @Param("city") String city,
            @Param("guests") int guests,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice);

    @Query(value = """
            SELECT * FROM properties p
            WHERE p.status = 'ACTIVE'
              AND NOT EXISTS (
                SELECT 1 FROM blocked_dates b
                WHERE b.property_id = p.id
                  AND b.start_date <= :checkOut
                  AND b.end_date >= :checkIn
              )
            """, nativeQuery = true)
    List<Property> findAllActiveAvailableInRange(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut);
}