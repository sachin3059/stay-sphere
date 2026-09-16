package com.staysphere.payment.exception;

import com.staysphere.common.exception.BadRequestException;

public class PaymentException extends BadRequestException {
    public PaymentException(String message) {
        super(message);
    }
}