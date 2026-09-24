package com.shecancode.attendence.Attendence.dao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

/**
 * One student's attendance, when the student is identified by the URL.
 * For recording, attendanceStatus is required; for an update, null fields keep their current value.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "One student's attendance entry")
public class AttendanceEntryRequest {

    @Schema(example = "LATE_PRESENT", allowableValues = {"PRESENT", "ABSENT", "ABSENT_COMMUNICATED", "LATE_PRESENT"})
    private AttendanceStatus attendanceStatus;

    @JsonFormat(pattern = "HH:mm:ss")
    @Schema(example = "09:20:00", type = "string",
            description = "Required for PRESENT and LATE_PRESENT; ignored for absences")
    private LocalTime checkInTime;

    @Size(max = 255)
    @Schema(example = "Traffic")
    private String remarks;
}
