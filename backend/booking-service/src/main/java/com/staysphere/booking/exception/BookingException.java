package com.staysphere.booking.exception;

import com.staysphere.common.exception.ConflictException;

public class BookingException extends ConflictException {

    public BookingException(String message) {
        super(message);
    }
}