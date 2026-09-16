package com.staysphere.availability.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_dates",
        indexes = {
                @Index(name = "idx_blocked_property",
                        columnList = "property_id, start_date, end_date")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class BlockedDate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlockReason reason;

    // bookingId if reason is BOOKED
    private String referenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum BlockReason {
        BOOKED,      // confirmed booking
        MAINTENANCE, // host blocked for maintenance
        HOST_BLOCK   // host manually blocked dates
    }
}