package com.shecancode.attendence.registration.dao;

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
public class StudentRequestDao {

    @NotBlank(message = "Student first name cannot be blank")
    @Size(min = 2, max = 50)
    private String studentFirstName;

    @NotBlank(message = "Student first name cannot be blank")
    private String studentLastName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String homeAddress;

    private String currentOccupation;

    @NotBlank(message = "Program selection is required")
    private String programName;

    private String cohortNumber;
}
