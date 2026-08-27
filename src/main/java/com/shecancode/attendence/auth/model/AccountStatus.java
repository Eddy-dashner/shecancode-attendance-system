package com.shecancode.attendence.auth.model;

/**
 * Onboarding/lifecycle state of a login account, distinct from the {@code enabled}
 * Spring Security flag and from the student's enrolment {@code Status}.
 *
 * Typical transitions:
 *  - Admin invites user            -> INVITED   (disabled, no password)
 *  - Student activates account     -> PROFILE_INCOMPLETE (enabled, password set)
 *  - Student completes profile     -> PROFILE_COMPLETE
 *  - Trainer/Admin activates       -> ACTIVE    (no profile step required)
 */
public enum AccountStatus {
    INVITED,
    PROFILE_INCOMPLETE,
    PROFILE_COMPLETE,
    ACTIVE
}
