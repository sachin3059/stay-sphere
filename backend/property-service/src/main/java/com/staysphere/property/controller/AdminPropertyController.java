package com.staysphere.property.controller;

import com.staysphere.common.ApiResponse;
import com.staysphere.property.dto.PropertyResponse;
import com.staysphere.property.dto.PropertyStatusRequest;
import com.staysphere.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/properties")
@RequiredArgsConstructor
public class AdminPropertyController {

    private final PropertyService propertyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PropertyResponse>>> listAll() {
        return ResponseEntity.ok(
                ApiResponse.success("Properties fetched",
                        propertyService.listAllForAdmin()));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PropertyResponse>> updateStatus(
            @PathVariable String id,
            @RequestBody PropertyStatusRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Listing status updated",
                        propertyService.adminUpdatePropertyStatus(
                                id, request.getStatus())));
    }
}
