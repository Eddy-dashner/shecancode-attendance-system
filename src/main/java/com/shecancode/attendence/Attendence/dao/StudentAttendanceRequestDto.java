package com.shecancode.attendence.Attendence.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class StudentAttendanceRequestDto {

    @NotNull
    private UUID studentId;

    @NotNull
    private AttendanceStatus attendanceStatus; // Consider using an Enum here (e.g., AttendanceStatus.PRESENT)

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkInTime;

    private String remarks;

}
