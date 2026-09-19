package com.staysphere.property.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.property.dto.ImageUploadResponse;
import com.staysphere.property.dto.PropertyRequest;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.dto.PropertyStatusRequest;
import com.staysphere.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @PreAuthorize("hasRole('HOST')")
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

    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> searchAdvanced(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(
                ApiResponse.success("Search results fetched successfully",
                        propertyService.searchWithPostgres(
                                query, city, guests, minPrice, maxPrice)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateProperty(
            @PathVariable String id,
            @RequestBody PropertyRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Property updated successfully",
                        propertyService.updateProperty(
                                id, request, getCurrentUserId())));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PropertyResponse>> updatePropertyStatus(
            @PathVariable String id,
            @RequestBody PropertyStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Property status updated successfully",
                        propertyService.updatePropertyStatus(
                                id, request.getStatus(), getCurrentUserId())));
    }

    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<ApiResponse<PropertyResponse>> deleteImage(
            @PathVariable String id,
            @RequestParam String url) {
        return ResponseEntity.ok(
                ApiResponse.success("Image removed successfully",
                        propertyService.removePropertyImage(
                                id, url, getCurrentUserId())));
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('HOST')")
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
