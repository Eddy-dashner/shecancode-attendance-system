package com.shecancode.attendence.Attendence.dao;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkAttendanceRequest {
    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    // Optional: These are usually redundant if they are in the URL path
    // private UUID programId;
    // private UUID cohortId;

    @NotNull(message = "Recorder ID is required")
    private UUID recordedById;

    @NotBlank(message = "Recorder name is required")
    private String recordedByName;

    @NotEmpty(message = "Student list cannot be empty")
    @Valid // Important: This triggers validation on the objects inside the list
    private List<StudentAttendanceRequestDto> students;
}
