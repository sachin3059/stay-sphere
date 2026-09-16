package com.staysphere.property.service;

import com.staysphere.common.exception.ResourceNotFoundException;
import com.staysphere.property.dto.ImageUploadResponse;
import com.staysphere.property.dto.PropertyRequest;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.entity.Property;
import com.staysphere.property.repository.PropertyRepository;
import com.staysphere.property.repository.PropertySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PropertySearchRepository propertySearchRepository;
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
        return mapToResponse(saved);
    }

    public PropertyResponse getProperty(String id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
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

    public List<PropertyResponse> searchWithPostgres(
            String query, String city, Integer guests,
            BigDecimal minPrice, BigDecimal maxPrice) {
        return propertySearchRepository.search(
                        blankToNull(query),
                        blankToNull(city),
                        guests,
                        minPrice,
                        maxPrice)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ImageUploadResponse uploadPropertyImages(
            String propertyId,
            List<MultipartFile> files,
            String hostId) {

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found"));

        if (!property.getHostId().equals(hostId)) {
            throw new ResourceNotFoundException(
                    "Not authorized to upload images for this property");
        }

        List<String> urls = cloudinaryService
                .uploadMultipleImages(files, "properties/" + propertyId);

        if (property.getImageUrls() == null) {
            property.setImageUrls(new java.util.ArrayList<>());
        }
        property.getImageUrls().addAll(urls);
        propertyRepository.save(property);

        log.info("Uploaded {} images for property {}", urls.size(), propertyId);

        return ImageUploadResponse.builder()
                .propertyId(propertyId)
                .imageUrls(property.getImageUrls())
                .totalImages(property.getImageUrls().size())
                .build();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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
}
