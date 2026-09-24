package com.shecancode.attendence.Attendence.Enum;

public enum AttendanceStatus {
    PRESENT, ABSENT, ABSENT_COMMUNICATED, LATE_PRESENT;

    public boolean isAbsence() {
        return this == ABSENT || this == ABSENT_COMMUNICATED;
    }

    public boolean requiresCheckInTime() {
        return this == PRESENT || this == LATE_PRESENT;
    }
}
