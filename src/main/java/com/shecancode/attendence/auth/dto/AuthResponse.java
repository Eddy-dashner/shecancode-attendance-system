package com.shecancode.attendence.auth.dto;

import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String username;
    private String fullName;
    private Role role;
    // Lets the frontend route: PROFILE_INCOMPLETE -> profile completion, else dashboard.
    private AccountStatus accountStatus;
}
