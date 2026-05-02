package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PropertyRepository extends JpaRepository<Property, String> {

    List<Property> findByHostId(String hostId);

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
}