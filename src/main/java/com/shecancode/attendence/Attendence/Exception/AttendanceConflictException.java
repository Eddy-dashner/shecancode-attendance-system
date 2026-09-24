package com.shecancode.attendence.Attendence.Exception;

/** Two people saved the same register at the same moment; the loser should reload and retry. */
public class AttendanceConflictException extends RuntimeException {
    public AttendanceConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
