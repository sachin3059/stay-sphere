package com.staysphere.payment.repository;

import com.staysphere.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByBookingId(String bookingId);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByGuestId(String guestId);

    boolean existsByIdempotencyKey(String idempotencyKey);
}