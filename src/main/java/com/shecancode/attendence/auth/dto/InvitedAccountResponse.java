package com.shecancode.attendence.auth.dto;

import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response returned to an admin after inviting a user. Deliberately excludes any
 * secret (no password, no activation token).
 */
@Data
@Builder
public class InvitedAccountResponse {
    private UUID userId;
    private String email;
    private String fullName;
    private Role role;
    private AccountStatus accountStatus;
}
