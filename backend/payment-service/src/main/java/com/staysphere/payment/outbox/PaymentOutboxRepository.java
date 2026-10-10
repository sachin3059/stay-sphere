package com.staysphere.payment.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface PaymentOutboxRepository extends JpaRepository<PaymentOutboxEvent, UUID> {

    @Query(value = """
            SELECT * FROM payment_outbox_events
            WHERE published_at IS NULL
            ORDER BY created_at
            LIMIT 50
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<PaymentOutboxEvent> findUnpublishedForUpdate();
}
