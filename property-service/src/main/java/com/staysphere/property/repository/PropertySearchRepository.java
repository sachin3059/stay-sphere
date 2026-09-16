package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PropertySearchRepository extends JpaRepository<Property, String> {

    @Query(value = """
            SELECT * FROM properties p
            WHERE p.status = 'ACTIVE'
              AND (:city IS NULL OR lower(p.city) = lower(:city))
              AND (:guests IS NULL OR p.max_guests >= :guests)
              AND (:minPrice IS NULL OR p.price_per_night >= :minPrice)
              AND (:maxPrice IS NULL OR p.price_per_night <= :maxPrice)
              AND (:query IS NULL OR p.search_vector @@ plainto_tsquery('english', :query)
                   OR p.title % :query)
            ORDER BY ts_rank(p.search_vector, plainto_tsquery('english', coalesce(:query, ''))) DESC
            LIMIT 50
            """, nativeQuery = true)
    List<Property> search(
            @Param("query") String query,
            @Param("city") String city,
            @Param("guests") Integer guests,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
