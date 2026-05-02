package com.staysphere.property.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String hostId;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String address;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    private Integer maxGuests;
    private Integer bedrooms;
    private Integer bathrooms;

    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    @ElementCollection
    @CollectionTable(name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "amenity")
    private List<String> amenities;

    @ElementCollection
    @CollectionTable(name = "property_images",
            joinColumns = @JoinColumn(name = "property_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = PropertyStatus.ACTIVE;
    }

    public enum PropertyType {
        APARTMENT, HOUSE, VILLA, STUDIO, CABIN, COTTAGE
    }

    public enum PropertyStatus {
        ACTIVE, INACTIVE, UNDER_REVIEW
    }
}