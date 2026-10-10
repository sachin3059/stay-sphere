package com.staysphere.property.service;

import com.staysphere.common.exception.BadRequestException;
import com.staysphere.common.exception.ResourceNotFoundException;
import com.staysphere.property.cache.PropertyActiveBloomFilter;
import com.staysphere.property.cache.PropertyCacheSupport;
import com.staysphere.property.cache.PropertySearchCachePayload;
import com.staysphere.property.dto.ImageUploadResponse;
import com.staysphere.property.dto.PropertyPageResponse;
import com.staysphere.property.dto.PropertyRequest;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.search.PropertySearchSort;
import com.staysphere.property.entity.Property;
import com.staysphere.pricing.entity.PricingRule;
import com.staysphere.pricing.repository.PricingRuleRepository;
import com.staysphere.property.repository.PropertyRepository;
import com.staysphere.property.repository.PropertySearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyService {

    private static final int MAX_PAGE_SIZE = 50;

    private final PropertyRepository propertyRepository;
    private final PropertySearchRepository propertySearchRepository;
    private final PricingRuleRepository pricingRuleRepository;
    private final CloudinaryService cloudinaryService;
    private final PropertyActiveBloomFilter propertyBloomFilter;
    private final PropertyCacheSupport propertyCache;

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
        propertyBloomFilter.onPropertyActivated(saved.getId());
        propertyCache.bumpSearchGeneration();
        return mapToResponse(saved);
    }

    public PropertyResponse getProperty(String id) {
        if (!propertyBloomFilter.mightBeActive(id)) {
            throw new ResourceNotFoundException("Property not found");
        }

        String cacheKey = propertyCache.detailKey(id);
        return propertyCache.redis().getOrLoad(
                cacheKey,
                PropertyResponse.class,
                propertyCache.detailTtl(),
                () -> loadActivePropertyFromDb(id));
    }

    public List<PropertyResponse> getHostProperties(String hostId) {
        return propertyRepository.findByHostId(hostId)
                .stream().map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PropertyPageResponse searchProperties(
            String city,
            int guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut,
            int page,
            int size,
            String sort) {
        return searchWithPostgres(
                null,
                city,
                guests,
                minPrice,
                maxPrice,
                checkIn,
                checkOut,
                page,
                size,
                sort);
    }

    public PropertyPageResponse searchWithPostgres(
            String query,
            String city,
            Integer guests,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDate checkIn,
            LocalDate checkOut,
            int page,
            int size,
            String sortParam) {
        validateStayDates(checkIn, checkOut);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalizedQuery = blankToNull(query);
        boolean hasTextQuery = normalizedQuery != null;
        PropertySearchSort sort = PropertySearchSort.fromParam(sortParam, hasTextQuery);

        String cacheKey = propertyCache.searchKey(
                normalizedQuery,
                city,
                guests,
                minPrice,
                maxPrice,
                checkIn,
                checkOut,
                safePage,
                safeSize,
                sort.name());
        PropertySearchCachePayload payload = propertyCache.redis().getOrLoad(
                cacheKey,
                PropertySearchCachePayload.class,
                propertyCache.searchTtl(),
                () -> new PropertySearchCachePayload(
                        loadSearchPage(
                                normalizedQuery,
                                city,
                                guests,
                                minPrice,
                                maxPrice,
                                checkIn,
                                checkOut,
                                sort,
                                safePage,
                                safeSize)));
        return payload.getPage();
    }

    public PropertyPageResponse getAllProperties(
            LocalDate checkIn,
            LocalDate checkOut,
            int page,
            int size,
            String sort) {
        return searchWithPostgres(
                null,
                null,
                null,
                null,
                null,
                checkIn,
                checkOut,
                page,
                size,
                sort);
    }

    private PropertyPageResponse loadSearchPage(
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
        var slice = propertySearchRepository.searchPage(
                query,
                city,
                guests,
                minPrice,
                maxPrice,
                checkIn,
                checkOut,
                sort,
                page,
                size);
        List<PropertyResponse> items = slice.items().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        int totalPages = slice.totalElements() == 0
                ? 0
                : (int) ((slice.totalElements() + size - 1) / size);
        return PropertyPageResponse.builder()
                .items(items)
                .page(page)
                .size(size)
                .totalElements(slice.totalElements())
                .totalPages(totalPages)
                .build();
    }

    private void validateStayDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null && checkOut == null) {
            return;
        }
        if (checkIn == null || checkOut == null) {
            throw new BadRequestException(
                    "Both checkIn and checkOut are required for date search");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("checkOut must be after checkIn");
        }
    }


    public List<PropertyResponse> listAllForAdmin() {
        return propertyRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PropertyResponse adminUpdatePropertyStatus(
            String propertyId,
            String status) {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required");
        }
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found"));
        property.setStatus(Property.PropertyStatus.valueOf(
                status.trim().toUpperCase()));
        Property saved = propertyRepository.save(property);
        onListingCatalogChanged(propertyId, saved.getStatus());
        return mapToResponse(saved);
    }

    public PropertyResponse updateProperty(
            String propertyId,
            PropertyRequest request,
            String hostId) {
        Property property = loadHostProperty(propertyId, hostId);

        property.setTitle(request.getTitle());
        property.setDescription(request.getDescription());
        property.setCity(request.getCity());
        property.setCountry(request.getCountry());
        property.setAddress(request.getAddress());
        if (request.getLatitude() != null) {
            property.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            property.setLongitude(request.getLongitude());
        }
        property.setPricePerNight(request.getPricePerNight());
        property.setMaxGuests(request.getMaxGuests());
        property.setBedrooms(request.getBedrooms());
        property.setBathrooms(request.getBathrooms());
        if (request.getPropertyType() != null) {
            property.setPropertyType(Property.PropertyType.valueOf(
                    request.getPropertyType().toUpperCase()));
        }
        if (request.getAmenities() != null) {
            property.setAmenities(new ArrayList<>(request.getAmenities()));
        }

        Property saved = propertyRepository.save(property);
        syncPricingBasePrice(propertyId, request.getPricePerNight());
        propertyCache.evictDetail(propertyId);
        propertyCache.bumpSearchGeneration();
        return mapToResponse(saved);
    }

    public PropertyResponse updatePropertyStatus(
            String propertyId,
            String status,
            String hostId) {
        Property property = loadHostProperty(propertyId, hostId);
        Property.PropertyStatus next = Property.PropertyStatus.valueOf(
                status.toUpperCase());
        if (next != Property.PropertyStatus.ACTIVE
                && next != Property.PropertyStatus.INACTIVE) {
            throw new IllegalArgumentException(
                    "Only ACTIVE or INACTIVE status is allowed");
        }
        property.setStatus(next);
        Property saved = propertyRepository.save(property);
        onListingCatalogChanged(propertyId, saved.getStatus());
        return mapToResponse(saved);
    }

    public PropertyResponse removePropertyImage(
            String propertyId,
            String imageUrl,
            String hostId) {
        Property property = loadHostProperty(propertyId, hostId);
        if (property.getImageUrls() == null
                || !property.getImageUrls().contains(imageUrl)) {
            throw new ResourceNotFoundException("Image not found on listing");
        }
        try {
            cloudinaryService.deleteImageByUrl(imageUrl);
        } catch (IllegalArgumentException ex) {
            log.warn("Skipping Cloudinary delete for url: {}", imageUrl);
        }
        property.getImageUrls().remove(imageUrl);
        Property saved = propertyRepository.save(property);
        propertyCache.evictDetail(propertyId);
        return mapToResponse(saved);
    }

    private Property loadHostProperty(String propertyId, String hostId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Property not found"));
        if (!property.getHostId().equals(hostId)) {
            throw new ResourceNotFoundException(
                    "Not authorized to modify this property");
        }
        return property;
    }

    private PropertyResponse loadActivePropertyFromDb(String id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));
        if (property.getStatus() != Property.PropertyStatus.ACTIVE) {
            throw new ResourceNotFoundException("Property not found");
        }
        return mapToResponse(property);
    }

    private void onListingCatalogChanged(
            String propertyId,
            Property.PropertyStatus status) {
        propertyCache.evictDetail(propertyId);
        propertyCache.bumpSearchGeneration();
        if (status == Property.PropertyStatus.ACTIVE) {
            propertyBloomFilter.onPropertyActivated(propertyId);
        } else {
            propertyBloomFilter.rebuild();
        }
    }

    private void syncPricingBasePrice(String propertyId, BigDecimal basePrice) {
        if (basePrice == null) {
            return;
        }
        pricingRuleRepository
                .findByPropertyIdAndStatus(
                        propertyId, PricingRule.RuleStatus.ACTIVE)
                .ifPresent(rule -> {
                    rule.setBasePrice(basePrice);
                    pricingRuleRepository.save(rule);
                });
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
        propertyCache.evictDetail(propertyId);

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
