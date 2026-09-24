package com.shecancode.attendence.Attendence.Exception;

/** A trainer tried to save a register for a day other than today (only admins can). */
public class AttendanceEditLockedException extends RuntimeException {
    public AttendanceEditLockedException(String message) {
        super(message);
    }
}
