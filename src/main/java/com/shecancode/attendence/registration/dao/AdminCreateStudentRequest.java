package com.shecancode.attendence.registration.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Admin-facing payload to enrol a student. The admin supplies only the email and
 * the program/cohort assignment; the student fills in the rest of their profile
 * after activating.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin payload to invite a student (email + program + cohort)")
public class AdminCreateStudentRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "aline.keza@example.com")
    private String email;

    @NotNull(message = "Program is required")
    @Schema(description = "ID of an existing program")
    private UUID programId;

    @NotNull(message = "Cohort is required")
    @Schema(description = "ID of an existing cohort (must be the program's cohort)")
    private UUID cohortId;
}
