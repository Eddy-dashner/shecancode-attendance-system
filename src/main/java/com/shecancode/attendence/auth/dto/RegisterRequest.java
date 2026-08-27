package com.shecancode.attendence.auth.dto;

import com.shecancode.attendence.auth.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Payload to register a new user (ADMIN only)")
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Schema(example = "trainer_jane", description = "Unique login username")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(example = "P@ssw0rd123", description = "Raw password; stored BCrypt-hashed")
    private String password;

    @NotBlank(message = "Full name is required")
    @Schema(example = "Jane Umutoni")
    private String fullName;

    @NotNull(message = "Role is required")
    @Schema(example = "ADMIN", allowableValues = {"ADMIN"},
            description = "Only ADMIN can be created here. Invite TRAINER/STUDENT via their invitation endpoints.")
    private Role role;
}
