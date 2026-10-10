package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import com.staysphere.property.search.PropertySearchSort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PropertySearchRepositoryCustom {

    record PropertySearchSlice(List<Property> items, long totalElements) {}

    PropertySearchSlice searchPage(
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut,
            PropertySearchSort sort,
            int page,
            int size);
}
