package com.staysphere.property.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.staysphere.property.entity.PropertyDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchService {

    private final ElasticsearchClient elasticsearchClient;

    // Full text + geo search
    public List<PropertyDocument> searchProperties(
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Double lat,
            Double lon,
            String radius) {

        try {
            List<Query> mustQueries = new ArrayList<>();
            List<Query> filterQueries = new ArrayList<>();

            // Only active properties
            filterQueries.add(Query.of(q -> q
                    .term(t -> t
                            .field("status")
                            .value("ACTIVE"))));

            // Full text search across title + description + amenities
            if (query != null && !query.isBlank()) {
                mustQueries.add(Query.of(q -> q
                        .multiMatch(m -> m
                                .query(query)
                                .fields("title^3", "description",
                                        "city^2", "amenities")
                                .fuzziness("AUTO"))));
            }

            // City filter
            if (city != null && !city.isBlank()) {
                filterQueries.add(Query.of(q -> q
                        .term(t -> t
                                .field("city")
                                .value(city))));
            }

            // Guest filter
            if (guests != null) {
                filterQueries.add(Query.of(q -> q
                        .range(r -> r
                                .field("maxGuests")
                                .gte(co.elastic.clients.json
                                        .JsonData.of(guests)))));
            }

            // Price range filter
            if (minPrice != null || maxPrice != null) {
                filterQueries.add(Query.of(q -> q
                        .range(r -> {
                            r.field("pricePerNight");
                            if (minPrice != null)
                                r.gte(co.elastic.clients.json
                                        .JsonData.of(minPrice));
                            if (maxPrice != null)
                                r.lte(co.elastic.clients.json
                                        .JsonData.of(maxPrice));
                            return r;
                        })));
            }

            // Geo distance filter
            if (lat != null && lon != null) {
                String distance = radius != null ? radius : "50km";
                filterQueries.add(Query.of(q -> q
                        .geoDistance(g -> g
                                .field("location")
                                .location(l -> l
                                        .latlon(ll -> ll
                                                .lat(lat)
                                                .lon(lon)))
                                .distance(distance))));
            }

            // Build final query
            Query finalQuery = Query.of(q -> q
                    .bool(b -> {
                        if (!mustQueries.isEmpty())
                            b.must(mustQueries);
                        if (!filterQueries.isEmpty())
                            b.filter(filterQueries);
                        if (mustQueries.isEmpty())
                            b.must(Query.of(mq -> mq
                                    .matchAll(ma -> ma)));
                        return b;
                    }));

            SearchResponse<PropertyDocument> response =
                    elasticsearchClient.search(s -> s
                                    .index("properties")
                                    .query(finalQuery)
                                    .size(50),
                            PropertyDocument.class);

            List<PropertyDocument> results = new ArrayList<>();
            for (Hit<PropertyDocument> hit : response.hits().hits()) {
                if (hit.source() != null) {
                    results.add(hit.source());
                }
            }

            log.info("ES search returned {} results", results.size());
            return results;

        } catch (IOException e) {
            log.error("Elasticsearch search error: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    // Index a property into Elasticsearch
    public void indexProperty(PropertyDocument document) {
        try {
            elasticsearchClient.index(i -> i
                    .index("properties")
                    .id(document.getId())
                    .document(document));
            log.info("Property indexed in ES: {}", document.getId());
        } catch (IOException e) {
            log.error("ES indexing error: {}", e.getMessage());
        }
    }

    // Delete a property from index
    public void deleteProperty(String propertyId) {
        try {
            elasticsearchClient.delete(d -> d
                    .index("properties")
                    .id(propertyId));
            log.info("Property deleted from ES: {}", propertyId);
        } catch (IOException e) {
            log.error("ES delete error: {}", e.getMessage());
        }
    }
}