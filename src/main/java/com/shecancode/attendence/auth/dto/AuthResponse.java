package com.shecancode.attendence.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// username/fullName/role/accountStatus are already embedded in accessToken's claims,
// so the client decodes the JWT instead of reading them off this response.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    // Access token lifetime in seconds, for the client to schedule its own refresh.
    private long expiresIn;
}
