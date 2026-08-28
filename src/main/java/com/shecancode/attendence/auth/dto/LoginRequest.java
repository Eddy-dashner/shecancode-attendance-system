package com.shecancode.attendence.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// ── Login ──────────────────────────────────────────────────────────────────────

@Data
@Schema(description = "Login credentials")
public class LoginRequest {
    @NotBlank(message = "Username is required")
    @Schema(example = "admin", description = "Seeded users: admin / trainer1")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(example = "admin123")
    private String password;
}
