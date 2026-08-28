package com.shecancode.attendence.registration.Exception;

/**
 * Thrown when the selected cohort does not match the selected program's cohort.
 */
public class CohortProgramMismatchException extends RuntimeException {
    public CohortProgramMismatchException(String message) {
        super(message);
    }
}
