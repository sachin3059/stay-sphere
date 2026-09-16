package com.staysphere.waitlist.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "waitlist_entries",
        indexes = {
                @Index(name = "idx_waitlist_property",
                        columnList = "property_id, check_in, check_out"),
                @Index(name = "idx_waitlist_guest",
                        columnList = "guest_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String propertyId;

    @Column(nullable = false)
    private String guestId;

    @Column(nullable = false)
    private LocalDate checkIn;

    @Column(nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer totalGuests;

    // Position in queue — lower = higher priority
    @Column(nullable = false)
    private Integer queuePosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status;

    // When slot was offered to this guest
    private LocalDateTime slotOfferedAt;

    // Slot expires at this time — guest must book within 30 mins
    private LocalDateTime slotExpiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = WaitlistStatus.WAITING;
    }

    public enum WaitlistStatus {
        WAITING,    // In queue, waiting for slot
        OFFERED,    // Slot offered, has 30 min to book
        BOOKED,     // Successfully booked
        EXPIRED,    // Didn't book within 30 min window
        CANCELLED   // Removed from waitlist
    }
}