package com.staysphere.property.repository;

import com.staysphere.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertySearchRepository
        extends JpaRepository<Property, String>, PropertySearchRepositoryCustom {
}
