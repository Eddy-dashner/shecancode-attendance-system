package com.shecancode.attendence.registration.dao;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Where a cohort is in its calendar, computed from its start and end dates.
 * currentWeek is 0 before the cohort starts and totalWeeks after it ends.
 */
public record CohortProgressResponse(
        UUID cohortId,
        LocalDate startDate,
        LocalDate endDate,
        int currentWeek,
        int totalWeeks,
        long daysElapsed,
        long daysRemaining,
        double percentComplete
) {}
