package com.shecancode.attendence.auth.dto;

import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private Role role;
    private AccountStatus accountStatus;
    private String bio;
    // Null when the user has not uploaded a picture.
    private String profilePictureUrl;
}
