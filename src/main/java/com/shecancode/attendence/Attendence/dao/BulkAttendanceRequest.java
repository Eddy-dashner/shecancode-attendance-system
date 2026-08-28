package com.shecancode.attendence.Attendence.dao;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Bulk attendance payload for a single date")
public class BulkAttendanceRequest {
    @NotNull(message = "Attendance date is required")
    @Schema(example = "2026-03-02")
    private LocalDate attendanceDate;

    // Optional: These are usually redundant if they are in the URL path
    // private UUID programId;
    // private UUID cohortId;

    @NotNull(message = "Recorder ID is required")
    @Schema(example = "550e8400-e29b-41d4-a716-446655440010", description = "ID of the trainer/admin recording attendance")
    private UUID recordedById;

    @NotBlank(message = "Recorder name is required")
    @Schema(example = "Default Trainer")
    private String recordedByName;

    @NotEmpty(message = "Student list cannot be empty")
    @Valid // Important: This triggers validation on the objects inside the list
    private List<StudentAttendanceRequestDto> students;
}
