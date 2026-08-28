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
    private String token;
    private String username;
    private String fullName;
    private Role role;
    private String tokenType;
    // Lets the frontend route: PROFILE_INCOMPLETE -> profile completion, else dashboard.
    private AccountStatus accountStatus;
}
