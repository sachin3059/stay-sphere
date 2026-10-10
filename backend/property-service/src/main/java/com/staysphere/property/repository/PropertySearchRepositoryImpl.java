package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import com.staysphere.property.search.PropertySearchSort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.hibernate.query.NativeQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public class PropertySearchRepositoryImpl implements PropertySearchRepositoryCustom {

    private static final String WHERE = """
            p.status = 'ACTIVE'
              AND (:city IS NULL OR lower(p.city) = lower(:city))
              AND (:guests IS NULL OR p.max_guests >= :guests)
              AND (:minPrice IS NULL OR p.price_per_night >= :minPrice)
              AND (:maxPrice IS NULL OR p.price_per_night <= :maxPrice)
              AND (:query IS NULL OR p.search_vector @@ plainto_tsquery('english', :query)
                   OR p.title % :query)
              AND (
                :checkIn IS NULL
                OR :checkOut IS NULL
                OR NOT EXISTS (
                  SELECT 1 FROM blocked_dates b
                  WHERE b.property_id = p.id
                    AND b.start_date <= :checkOut
                    AND b.end_date >= :checkIn
                )
              )
            """;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PropertySearchSlice searchPage(
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut,
            PropertySearchSort sort,
            int page,
            int size) {
        boolean hasTextQuery = query != null && !query.isBlank();
        String orderBy = sort.orderByClause(hasTextQuery);

        String countSql = "SELECT COUNT(*) FROM properties p WHERE " + WHERE;
        Query countQuery = entityManager.createNativeQuery(countSql);
        bindFilters(countQuery, query, city, guests, minPrice, maxPrice, checkIn, checkOut);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        String dataSql = "SELECT * FROM properties p WHERE " + WHERE
                + " ORDER BY " + orderBy
                + " LIMIT :limit OFFSET :offset";
        Query dataQuery = entityManager.createNativeQuery(dataSql, Property.class);
        bindFilters(dataQuery, query, city, guests, minPrice, maxPrice, checkIn, checkOut);
        dataQuery.setParameter("limit", size);
        dataQuery.setParameter("offset", (long) page * size);

        @SuppressWarnings("unchecked")
        List<Property> items = dataQuery.getResultList();
        return new PropertySearchSlice(items, total);
    }

    private static void bindFilters(
            Query q,
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut) {
        NativeQuery<?> nq = q.unwrap(NativeQuery.class);
        nq.setParameter("query", blankToNull(query), String.class);
        nq.setParameter("city", blankToNull(city), String.class);
        nq.setParameter("guests", guests, Integer.class);
        nq.setParameter("minPrice", minPrice, BigDecimal.class);
        nq.setParameter("maxPrice", maxPrice, BigDecimal.class);
        nq.setParameter("checkIn", checkIn, LocalDate.class);
        nq.setParameter("checkOut", checkOut, LocalDate.class);
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
