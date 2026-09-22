package com.shecancode.attendence.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Login credentials")
public class LoginRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Schema(example = "admin@shecancode.org", description = "Email address of the account (admin, trainer or student). Seeded: admin@shecancode.org / trainer1@shecancode.org")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(example = "admin123")
    private String password;
}
