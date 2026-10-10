package com.staysphere.property.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "availability",
        indexes = @Index(name = "idx_availability_property",
                columnList = "property_id, start_date, end_date"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private AvailabilityStatus status;

    public enum AvailabilityStatus {
        BLOCKED, BOOKED
    }
}