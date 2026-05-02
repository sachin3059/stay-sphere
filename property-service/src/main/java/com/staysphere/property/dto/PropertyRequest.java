package com.staysphere.property.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PropertyRequest {
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
    private List<String> amenities;
}