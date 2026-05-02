package com.staysphere.property.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.property.dto.ImageUploadResponse;
import com.staysphere.property.dto.PropertyRequest;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    private String getCurrentUserId() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PropertyResponse>> createProperty(
            @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Property created successfully",
                        propertyService.createProperty(
                                request, getCurrentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyResponse>> getProperty(
            @PathVariable String id) {
        return ResponseEntity.ok(
                ApiResponse.success("Property fetched successfully",
                        propertyService.getProperty(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getAllProperties() {
        return ResponseEntity.ok(
                ApiResponse.success("Properties fetched successfully",
                        propertyService.getAllProperties()));
    }

    @GetMapping("/host")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> getHostProperties() {
        return ResponseEntity.ok(
                ApiResponse.success("Host properties fetched successfully",
                        propertyService.getHostProperties(
                                getCurrentUserId())));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> search(
            @RequestParam String city,
            @RequestParam(defaultValue = "1") int guests,
            @RequestParam(defaultValue = "0") BigDecimal minPrice,
            @RequestParam(defaultValue = "99999") BigDecimal maxPrice) {
        return ResponseEntity.ok(
                ApiResponse.success("Search results fetched successfully",
                        propertyService.searchProperties(
                                city, guests, minPrice, maxPrice)));
    }

    @GetMapping("/search/es")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> searchWithEs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) String radius) {
        return ResponseEntity.ok(
                ApiResponse.success("Elasticsearch results fetched successfully",
                        propertyService.searchWithElasticsearch(
                                query, city, guests,
                                minPrice, maxPrice,
                                lat, lon, radius)));
    }

    @PostMapping("/admin/reindex")
    public ResponseEntity<ApiResponse<String>> reindexAll() {
        propertyService.reindexAllProperties();
        return ResponseEntity.ok(
                ApiResponse.success("All properties reindexed successfully"));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> uploadImages(
            @PathVariable String id,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.ok(
                ApiResponse.success("Images uploaded successfully",
                        propertyService.uploadPropertyImages(
                                id, files, getCurrentUserId())));
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> health() {
        return ResponseEntity.ok(
                ApiResponse.success("Property service is running"));
    }
}