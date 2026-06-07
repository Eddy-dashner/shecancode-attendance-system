package com.shecancode.attendence.Attendence.Event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceEvent {

    private UUID eventId;

    private UUID attendanceId;

    private UUID studentId;

    private UUID programId;

    private UUID cohortId;

    // 1. Formats as "yyyy-MM-dd" (e.g., "2026-05-28")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    private AttendanceStatus attendanceStatus;

    // 2. Formats as "HH:mm:ss" (e.g., "09:15:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    // 3. Formats as standard UTC ISO-8601 string (e.g., "2026-05-28T06:30:00Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
