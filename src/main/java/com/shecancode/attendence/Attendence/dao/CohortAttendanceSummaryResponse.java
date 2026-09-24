package com.shecancode.attendence.Attendence.dao;

import com.shecancode.attendence.Attendence.Enum.AlertType;
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

    /** attendance counts only this cohort's sessions; all zero before the first session. */
    public record StudentSummary(
            UUID studentId,
            String studentName,
            AttendanceSummaryDto attendance,
            List<AlertType> activeAlerts
    ) {}
}
