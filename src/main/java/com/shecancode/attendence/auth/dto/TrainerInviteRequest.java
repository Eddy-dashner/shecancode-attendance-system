package com.shecancode.attendence.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Admin payload to invite a trainer (email only; password set on activation)")
public class TrainerInviteRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Schema(example = "trainer.jane@example.com")
    private String email;

    @NotBlank(message = "Full name is required")
    @Schema(example = "Jane Umutoni")
    private String fullName;
}
