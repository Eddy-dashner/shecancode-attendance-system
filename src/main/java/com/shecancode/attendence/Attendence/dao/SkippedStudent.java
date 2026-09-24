package com.shecancode.attendence.Attendence.dao;

import java.util.UUID;

public record SkippedStudent(UUID studentId, Reason reason) {

    public enum Reason {
        NOT_FOUND,
        NOT_IN_COHORT,
        DROPPED_OUT
    }
}
