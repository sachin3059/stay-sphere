package com.staysphere.waitlist.exception;

import com.staysphere.common.exception.ConflictException;

public class WaitlistException extends ConflictException {
    public WaitlistException(String message) {
        super(message);
    }
}