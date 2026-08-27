package com.shecancode.attendence.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Payload for a student to activate their account and set a password")
public class ActivateAccountRequest {

    @NotBlank(message = "Activation token is required")
    @Schema(description = "One-time token from the activation email link")
    private String token;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(example = "P@ssw0rd123", description = "Raw password; stored BCrypt-hashed")
    private String password;

    @NotBlank(message = "Password confirmation is required")
    @Schema(example = "P@ssw0rd123", description = "Must match password")
    private String confirmPassword;
}
