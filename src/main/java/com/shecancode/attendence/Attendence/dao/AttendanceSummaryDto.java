package com.shecancode.attendence.Attendence.dao;

import com.shecancode.attendence.Attendence.Enum.ProgressColor;
import com.shecancode.attendence.Attendence.Service.AttendanceScore;

/** A student's attendance counts and score, without the individual records. */
public record AttendanceSummaryDto(
        int sessionsRecorded,
        int present,
        int late,
        int absent,
        int absentCommunicated,
        int consecutiveAbsences,
        double attendancePoints,
        double attendancePercentage,
        ProgressColor color
) {
    public static AttendanceSummaryDto of(AttendanceScore score) {
        return new AttendanceSummaryDto(score.sessionsRecorded(), score.present(), score.late(), score.absent(),
                score.absentCommunicated(), score.consecutiveAbsences(), score.pointsKept(), score.percentage(),
                score.color());
    }
}
