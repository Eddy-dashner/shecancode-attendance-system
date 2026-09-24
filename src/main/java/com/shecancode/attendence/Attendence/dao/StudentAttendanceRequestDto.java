package com.shecancode.attendence.Attendence.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalTime;
import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
@Schema(description = "Per-student attendance entry")
public class StudentAttendanceRequestDto {

    @NotNull
    @Schema(example = "550e8400-e29b-41d4-a716-446655440011")
    private UUID studentId;

    @NotNull
    @Schema(example = "PRESENT", allowableValues = {"PRESENT", "ABSENT", "ABSENT_COMMUNICATED", "LATE_PRESENT"})
    private AttendanceStatus attendanceStatus;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(example = "09:00:00", type = "string",
            description = "Required for PRESENT and LATE_PRESENT; ignored for absences")
    private LocalTime checkInTime;

    @Size(max = 255)
    @Schema(example = "On time")
    private String remarks;

}
