package com.shecancode.attendence.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Payload to exchange a refresh token for a new access token")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "The refresh token returned by login, activate, or a previous refresh call")
    private String refreshToken;
}
