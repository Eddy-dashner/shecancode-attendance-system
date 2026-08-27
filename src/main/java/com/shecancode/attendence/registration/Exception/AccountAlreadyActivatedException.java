package com.shecancode.attendence.registration.Exception;

/**
 * Thrown when attempting to (re)invite or activate an account that is already active.
 */
public class AccountAlreadyActivatedException extends RuntimeException {
    public AccountAlreadyActivatedException(String message) {
        super(message);
    }
}
