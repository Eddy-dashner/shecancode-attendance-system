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

    private int version;

    private UUID eventId;

    private UUID attendanceId;

    private UUID studentId;

    private UUID cohortId;

    private UUID programId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate attendanceDate;

    private AttendanceStatus attendanceStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
