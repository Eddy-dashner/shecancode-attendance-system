package com.shecancode.attendence.registration.Exception;

/**
 * Thrown when an activation token is invalid, expired, or already used.
 * Deliberately generic so responses never reveal which condition failed.
 */
public class ActivationTokenException extends RuntimeException {
    public ActivationTokenException(String message) {
        super(message);
    }
}
