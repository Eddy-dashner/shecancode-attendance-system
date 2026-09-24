package com.shecancode.attendence.Attendence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Attendance records for a date range with totals, for a cohort or a whole program. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceReportResponse {
    private UUID programId;
    // Null for a program-wide report without a cohort filter.
    private UUID cohortId;
    private LocalDate from;
    private LocalDate to;
    private Totals totals;
    // Newest first, then by student name.
    private List<AttendanceResponse> records;

    /**
     * presentPercent counts PRESENT and LATE_PRESENT; absentPercent counts ABSENT and
     * ABSENT_COMMUNICATED. Both are 0 when there are no records.
     */
    public record Totals(int records, int present, int late, int absent, int absentCommunicated,
                         double presentPercent, double absentPercent) {}
}
