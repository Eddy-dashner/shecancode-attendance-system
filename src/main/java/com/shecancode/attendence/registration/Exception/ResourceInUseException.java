package com.shecancode.attendence.registration.Exception;

/** A delete was refused because other records still depend on this one. */
public class ResourceInUseException extends RuntimeException {
    public ResourceInUseException(String message) {
        super(message);
    }
}
