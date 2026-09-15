package com.shecancode.attendence.registration.Exception;

/**
 * Thrown when a refresh token is unknown, expired, revoked, or belongs to a
 * disabled account. Deliberately generic so responses never reveal which
 * condition failed.
 */
public class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
