package com.staysphere.property.repository;

import com.staysphere.property.entity.PropertyDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface PropertySearchRepository
        extends ElasticsearchRepository<PropertyDocument, String> {

    List<PropertyDocument> findByCityIgnoreCaseAndStatus(
            String city, String status);

    List<PropertyDocument> findByStatus(String status);
}