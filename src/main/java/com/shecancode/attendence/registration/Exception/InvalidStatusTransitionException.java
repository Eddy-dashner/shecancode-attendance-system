package com.shecancode.attendence.registration.Exception;

/** A status change that the lifecycle does not allow (e.g. GRADUATED -> ACTIVE). */
public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
