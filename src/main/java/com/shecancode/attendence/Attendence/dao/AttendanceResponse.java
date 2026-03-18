package com.shecancode.attendence.Attendence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {
    
    private UUID attendanceId;

    private UUID studentId;
    private String studentName;

    private UUID cohortId;
    private String cohortNumber;

    private UUID programId;
    private String programName;

    private String attendanceStatus;

    private LocalTime checkInTime;

    private Integer daysRemainingUntilGraduation;

    private String remarks;
    private LocalDate attendanceRecordedDate;
    private LocalDate graduationDate;
    private String participationStatus;

    private UUID recordedById;
    private String recordedByName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
