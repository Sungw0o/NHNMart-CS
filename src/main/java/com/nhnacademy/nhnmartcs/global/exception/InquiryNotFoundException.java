package com.nhnacademy.nhnmartcs.global.exception;

public class InquiryNotFoundException extends RuntimeException {
    /**
     * Constructs an InquiryNotFoundException with the specified detail message.
     *
     * @param message the detail message describing the not-found condition
     */
    public InquiryNotFoundException(String message) {
        super(message);
    }
}