package com.staysphere.property.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyResponse {
    private String id;
    private String hostId;
    private String title;
    private String description;
    private String city;
    private String country;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal pricePerNight;
    private Integer maxGuests;
    private Integer bedrooms;
    private Integer bathrooms;
    private String propertyType;
    private String status;
    private List<String> amenities;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
}