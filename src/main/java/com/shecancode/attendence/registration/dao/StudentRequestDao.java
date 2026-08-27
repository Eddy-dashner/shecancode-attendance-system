package com.shecancode.attendence.registration.dao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student registration payload")
public class StudentRequestDao {

    @NotBlank(message = "Student first name cannot be blank")
    @Size(min = 2, max = 50)
    @Schema(example = "Aline")
    private String studentFirstName;

    @NotBlank(message = "Student first name cannot be blank")
    @Schema(example = "Keza")
    private String studentLastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    @Schema(example = "+250780000001")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "aline.keza@example.com")
    private String email;

    @Schema(example = "Kigali, Gasabo")
    private String homeAddress;

    @Schema(example = "Student")
    private String currentOccupation;

    @NotBlank(message = "Program selection is required")
    @Schema(example = "Backend", description = "Name of an existing program")
    private String programName;

    @Schema(example = "Cohort-10", description = "Ignored on this endpoint; the path {cohortNumber} is authoritative")
    private String cohortNumber;
}
