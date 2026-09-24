package com.shecancode.attendence.registration.Exception;

/** The student's status (e.g. INACTIVE, GRADUATED) does not allow this action. */
public class StudentNotActiveException extends RuntimeException {
    public StudentNotActiveException(String message) {
        super(message);
    }
}
