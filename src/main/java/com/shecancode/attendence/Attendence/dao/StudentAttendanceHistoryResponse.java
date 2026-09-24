package com.shecancode.attendence.Attendence.dao;

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
public class StudentAttendanceHistoryResponse {
    private UUID studentId;
    private String studentName;
    private UUID programId;
    private UUID cohortId;

    private int sessionsRecorded;
    private int absent;
    private int absentCommunicated;
    private int late;
    private int consecutiveAbsences;
    private double attendancePoints;
    private double attendancePercentage;
    private ProgressColor color;

    // Newest first.
    private List<AttendanceResponse> records;
}
