package com.shecancode.attendence.registration.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Student-facing payload to complete their profile after activating the account.
 * Email, cohort and program are already set by the admin and are not editable here.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student profile completion payload")
public class CompleteProfileRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50)
    @Schema(example = "Aline")
    private String studentFirstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50)
    @Schema(example = "Keza")
    private String studentLastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(example = "+250780000001")
    private String phoneNumber;

    @Schema(example = "Kigali, Gasabo")
    private String homeAddress;

    @Schema(example = "Student")
    private String currentOccupation;
}
