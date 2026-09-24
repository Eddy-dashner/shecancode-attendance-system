package com.shecancode.attendence.Attendence.dao;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * The register for a cohort on a date, listing every enrolled student.
 * Students not marked yet have a null status, so the UI can pre-fill the form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CohortRegisterResponse {
    // Null when nobody has taken attendance for this date yet.
    private UUID sessionId;
    private UUID programId;
    private UUID cohortId;
    private LocalDate sessionDate;
    private String takenByName;
    private List<Entry> entries;

    public record Entry(
            UUID studentId,
            String studentName,
            UUID attendanceId,
            AttendanceStatus attendanceStatus,
            LocalTime checkInTime,
            String remarks
    ) {}
}
