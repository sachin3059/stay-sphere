package com.staysphere.property.service;

import com.staysphere.property.dto.ImageUploadResponse;
import com.staysphere.property.dto.PropertyRequest;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.entity.Property;
import com.staysphere.property.entity.PropertyDocument;
import com.staysphere.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final ElasticsearchService elasticsearchService;
    private final CloudinaryService cloudinaryService;

    public PropertyResponse createProperty(PropertyRequest request, String hostId) {
        Property property = Property.builder()
                .hostId(hostId)
                .title(request.getTitle())
                .description(request.getDescription())
                .city(request.getCity())
                .country(request.getCountry())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .pricePerNight(request.getPricePerNight())
                .maxGuests(request.getMaxGuests())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .propertyType(Property.PropertyType.valueOf(
                        request.getPropertyType().toUpperCase()))
                .amenities(request.getAmenities())
                .status(Property.PropertyStatus.ACTIVE)
                .build();

        Property saved = propertyRepository.save(property);

        elasticsearchService.indexProperty(
                mapToDocument(saved));

        return mapToResponse(saved);
    }

    public PropertyResponse getProperty(String id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        return mapToResponse(property);
    }

    public List<PropertyResponse> getHostProperties(String hostId) {
        return propertyRepository.findByHostId(hostId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> searchProperties(
            String city, int guests,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return propertyRepository.searchProperties(city, guests, minPrice, maxPrice)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> searchWithElasticsearch(
            String query, String city, Integer guests,
            BigDecimal minPrice, BigDecimal maxPrice,
            Double lat, Double lon, String radius) {

        List<PropertyDocument> docs =
                elasticsearchService.searchProperties(
                        query, city, guests,
                        minPrice, maxPrice,
                        lat, lon, radius);

        if (docs.isEmpty()) {
            // Fallback to PostgreSQL if ES returns nothing
            return searchProperties(city != null ? city : "",
                    guests != null ? guests : 1,
                    minPrice != null ? minPrice : BigDecimal.ZERO,
                    maxPrice != null ? maxPrice :
                            new BigDecimal("99999"));
        }

        return docs.stream()
                .map(this::mapDocumentToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void reindexAllProperties() {
        List<Property> all = propertyRepository.findAll();
        for (Property p : all) {
            elasticsearchService.indexProperty(mapToDocument(p));
        }
        log.info("Reindexed {} properties into Elasticsearch",
                all.size());
    }

    public ImageUploadResponse uploadPropertyImages(
            String propertyId,
            List<MultipartFile> files,
            String hostId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new RuntimeException("Property not found"));

        if (!property.getHostId().equals(hostId)) {
            throw new RuntimeException(
                    "Not authorized to upload images for this property");
        }

        List<String> urls = cloudinaryService
                .uploadMultipleImages(files, "properties/" + propertyId);

        if (property.getImageUrls() == null) {
            property.setImageUrls(new java.util.ArrayList<>());
        }
        property.getImageUrls().addAll(urls);
        propertyRepository.save(property);

        // Update ES index with new image URLs
        elasticsearchService.indexProperty(mapToDocument(property));

        log.info("Uploaded {} images for property {}",
                urls.size(), propertyId);

        return ImageUploadResponse.builder()
                .propertyId(propertyId)
                .imageUrls(property.getImageUrls())
                .totalImages(property.getImageUrls().size())
                .build();
    }

    private PropertyResponse mapToResponse(Property p) {
        return PropertyResponse.builder()
                .id(p.getId())
                .hostId(p.getHostId())
                .title(p.getTitle())
                .description(p.getDescription())
                .city(p.getCity())
                .country(p.getCountry())
                .address(p.getAddress())
                .latitude(p.getLatitude())
                .longitude(p.getLongitude())
                .pricePerNight(p.getPricePerNight())
                .maxGuests(p.getMaxGuests())
                .bedrooms(p.getBedrooms())
                .bathrooms(p.getBathrooms())
                .propertyType(p.getPropertyType() != null ?
                        p.getPropertyType().name() : null)
                .status(p.getStatus().name())
                .amenities(p.getAmenities())
                .createdAt(p.getCreatedAt())
                .imageUrls(p.getImageUrls())
                .build();
    }


    private PropertyDocument mapToDocument(Property p) {
        PropertyDocument doc = PropertyDocument.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .city(p.getCity())
                .country(p.getCountry())
                .address(p.getAddress())
                .pricePerNight(p.getPricePerNight())
                .maxGuests(p.getMaxGuests())
                .bedrooms(p.getBedrooms())
                .bathrooms(p.getBathrooms())
                .propertyType(p.getPropertyType() != null ?
                        p.getPropertyType().name() : null)
                .amenities(p.getAmenities())
                .status(p.getStatus().name())
                .hostId(p.getHostId())
                .build();

        if (p.getLatitude() != null && p.getLongitude() != null) {
            doc.setLocation(new GeoPoint(
                    p.getLatitude(), p.getLongitude()));
        }

        return doc;
    }

    private PropertyResponse mapDocumentToResponse(
            PropertyDocument doc) {
        return PropertyResponse.builder()
                .id(doc.getId())
                .hostId(doc.getHostId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .city(doc.getCity())
                .country(doc.getCountry())
                .address(doc.getAddress())
                .latitude(doc.getLocation() != null ?
                        doc.getLocation().getLat() : null)
                .longitude(doc.getLocation() != null ?
                        doc.getLocation().getLon() : null)
                .pricePerNight(doc.getPricePerNight())
                .maxGuests(doc.getMaxGuests())
                .bedrooms(doc.getBedrooms())
                .bathrooms(doc.getBathrooms())
                .propertyType(doc.getPropertyType())
                .status(doc.getStatus())
                .amenities(doc.getAmenities())
                .build();
    }
}