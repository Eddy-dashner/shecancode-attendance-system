package com.shecancode.attendence.registration.Exception;

/** A change was attempted on a closed (read-only) program or cohort. */
public class ReadOnlyException extends RuntimeException {
    public ReadOnlyException(String message) {
        super(message);
    }
}
