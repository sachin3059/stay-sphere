package com.staysphere.payment.gateway;

import com.staysphere.payment.config.PaymentProperties;
import com.staysphere.payment.entity.Payment;
import com.staysphere.payment.exception.PaymentException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "payment.gateway", havingValue = "stripe")
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private final PaymentProperties paymentProperties;

    @Override
    public boolean isSimulated() {
        return false;
    }

    @Override
    public GatewayResult charge(Payment payment) {
        throw new PaymentException(
                "Stripe uses PaymentIntent flow. Call /api/payments/stripe/intent instead.");
    }

    public PaymentIntent createPaymentIntent(Payment payment) {
        ensureConfigured();
        try {
            long amountMinor = toMinorUnits(payment.getAmount(), payment.getCurrency());
            Map<String, String> metadata = new HashMap<>();
            metadata.put("paymentId", payment.getId());
            metadata.put("bookingId", payment.getBookingId());
            metadata.put("guestId", payment.getGuestId());
            metadata.put("idempotencyKey", payment.getIdempotencyKey());

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountMinor)
                    .setCurrency(payment.getCurrency().toLowerCase())
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .setAllowRedirects(
                                            PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                                    .build())
                    .putAllMetadata(metadata)
                    .build();

            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new PaymentException("Stripe PaymentIntent creation failed: " + e.getMessage());
        }
    }

    public PaymentIntent retrievePaymentIntent(String paymentIntentId) {
        ensureConfigured();
        try {
            return PaymentIntent.retrieve(
                    paymentIntentId,
                    PaymentIntentRetrieveParams.builder().build(),
                    null);
        } catch (StripeException e) {
            throw new PaymentException("Stripe PaymentIntent retrieve failed: " + e.getMessage());
        }
    }

    @Override
    public GatewayResult refund(Payment payment) {
        ensureConfigured();
        if (payment.getTransactionId() == null || payment.getTransactionId().isBlank()) {
            return GatewayResult.builder()
                    .success(false)
                    .failureReason("Missing Stripe payment intent id")
                    .build();
        }
        try {
            Refund refund = Refund.create(RefundCreateParams.builder()
                    .setPaymentIntent(payment.getTransactionId())
                    .build());
            return GatewayResult.builder()
                    .success(true)
                    .transactionId(refund.getId())
                    .build();
        } catch (StripeException e) {
            return GatewayResult.builder()
                    .success(false)
                    .failureReason(e.getMessage())
                    .build();
        }
    }

    public String publishableKey() {
        return paymentProperties.getStripe().getPublishableKey();
    }

    public String webhookSecret() {
        return paymentProperties.getStripe().getWebhookSecret();
    }

    private void ensureConfigured() {
        if (paymentProperties.getStripe().getSecretKey() == null
                || paymentProperties.getStripe().getSecretKey().isBlank()) {
            throw new PaymentException("STRIPE_SECRET_KEY is not configured");
        }
    }

    static long toMinorUnits(BigDecimal amount, String currency) {
        int fractionDigits = "JPY".equalsIgnoreCase(currency) ? 0 : 2;
        BigDecimal scaled = amount.setScale(fractionDigits, RoundingMode.HALF_UP);
        if (fractionDigits == 0) {
            return scaled.longValue();
        }
        return scaled.movePointRight(2).longValueExact();
    }
}
