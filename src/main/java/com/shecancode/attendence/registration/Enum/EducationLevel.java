package com.shecancode.attendence.registration.Enum;

public enum EducationLevel {
    HIGH_SCHOOL(false),
    TECHNICAL_SCHOOL(false),
    YEAR_1_UNIVERSITY(true),
    YEAR_2_UNIVERSITY(true),
    YEAR_3_UNIVERSITY(true),
    YEAR_4_UNIVERSITY(true),
    FINAL_YEAR_UNIVERSITY(true),
    BACHELORS(true),
    MASTERS(true),
    PHD(true),
    OTHER(false);

    // University-level answers must also name the university / institution.
    private final boolean requiresInstitution;

    EducationLevel(boolean requiresInstitution) {
        this.requiresInstitution = requiresInstitution;
    }

    public boolean requiresInstitution() {
        return requiresInstitution;
    }
}
