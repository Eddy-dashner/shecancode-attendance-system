package com.shecancode.attendence.Attendence.dao;

import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Enum.ProgressColor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CohortAttendanceSummaryResponse {
    private UUID programId;
    private UUID cohortId;
    private String cohortNumber;
    private List<StudentSummary> students;

    /** Progress fields are null for a student with no attendance recorded yet. */
    public record StudentSummary(
            UUID studentId,
            String studentName,
            Double attendancePoints,
            Double attendancePercentage,
            ProgressColor color,
            Integer consecutiveAbsences,
            List<AlertType> activeAlerts
    ) {}
}
