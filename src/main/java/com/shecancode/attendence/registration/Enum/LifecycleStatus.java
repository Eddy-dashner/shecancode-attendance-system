package com.shecancode.attendence.registration.Enum;

/**
 * Status of a program or cohort. CLOSED is read-only: it can be viewed but not
 * changed (no updates, students or attendance) until an admin reopens it.
 * A closed program makes all of its cohorts read-only too.
 */
public enum LifecycleStatus {
    OPEN,
    CLOSED
}
