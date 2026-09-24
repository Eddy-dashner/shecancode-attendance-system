package com.shecancode.attendence.Attendence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Result of saving a register: what was saved, what was skipped and why, and any alerts raised. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceRegisterResponse {
    private UUID sessionId;
    private UUID programId;
    private UUID cohortId;
    private LocalDate sessionDate;
    private String takenByName;

    private List<AttendanceResponse> records;
    private List<SkippedStudent> skipped;
    private List<AttendanceAlertResponse> alertsRaised;
}
