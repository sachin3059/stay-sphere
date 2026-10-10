package com.staysphere.booking.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "booking_processed_events")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedEvent {

    @Id
    private String eventId;

    private String consumerName;
}
