package com.nhnacademy.nhnmartcs.global.exception;

public class LoginFailedException extends RuntimeException {
    /**
     * Create a LoginFailedException with the specified detail message.
     *
     * @param message the detail message describing the login failure
     */
    public LoginFailedException(String message) {
        super(message);
    }
}