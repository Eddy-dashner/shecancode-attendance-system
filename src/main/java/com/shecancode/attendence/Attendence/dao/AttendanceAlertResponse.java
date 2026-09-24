package com.shecancode.attendence.Attendence.dao;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceAlertResponse {
    private UUID alertId;
    private AlertType alertType;
    private AlertStatus status;
    private int absenceCount;

    private UUID studentId;
    private String studentName;
    private UUID cohortId;
    private String cohortNumber;
    private UUID programId;
    private String programName;

    private LocalDate triggeredOnDate;
    private String triggeredByName;
    private Instant createdAt;
    private Instant resolvedAt;
    private Instant studentNotifiedAt;
    private Instant trainerNotifiedAt;
}
