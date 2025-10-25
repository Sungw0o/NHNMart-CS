package com.nhnacademy.nhnmartcs.global.exception;

public class InquiryAccessDeniedException extends RuntimeException {
    /**
     * Constructs a new InquiryAccessDeniedException with the specified detail message.
     *
     * @param message the detail message explaining the access denial
     */
    public InquiryAccessDeniedException(String message) {
        super(message);
    }
}