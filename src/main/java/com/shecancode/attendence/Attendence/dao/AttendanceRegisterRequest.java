package com.shecancode.attendence.Attendence.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "The register for one cohort on one date. Saving again replaces the entries for the listed students.")
public class AttendanceRegisterRequest {

    @NotEmpty(message = "Student list cannot be empty")
    @Valid
    private List<StudentAttendanceRequestDto> students;
}
