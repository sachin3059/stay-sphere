package com.staysphere.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_guest", columnList = "guest_id"),
                @Index(name = "idx_booking_property", columnList = "property_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private String guestId;

    @Column(nullable = false)
    private String hostId;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer totalGuests;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private BigDecimal pricePerNight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(unique = true)
    private String idempotencyKey;

    private String propertyLockKey;
    private String propertyLockToken;
    private LocalDateTime pendingExpiresAt;

    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, EXPIRED
    }
}