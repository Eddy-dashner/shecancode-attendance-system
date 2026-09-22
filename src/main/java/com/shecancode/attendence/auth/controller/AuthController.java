package com.shecancode.attendence.auth.controller;

import com.shecancode.attendence.auth.dto.ActivateAccountRequest;
import com.shecancode.attendence.auth.dto.AuthResponse;
import com.shecancode.attendence.auth.dto.LoginRequest;
import com.shecancode.attendence.auth.dto.RefreshTokenRequest;
import com.shecancode.attendence.auth.dto.RegisterRequest;
import com.shecancode.attendence.auth.dto.ResendActivationRequest;
import com.shecancode.attendence.auth.service.ActivationService;
import com.shecancode.attendence.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final ActivationService activationService;

    // The very first ADMIN must be seeded via data.sql.
    @PostMapping("/register")
    @Operation(tags = {"Authentication"}, summary = "Register a new ADMIN (ADMIN only)",
            description = "Password-based creation of an ADMIN account (bootstrap / adding admins) and returns a JWT. " +
                    "TRAINER and STUDENT accounts are created via the invitation flow (POST /api/v1/trainers and " +
                    "POST /api/v1/students) and are rejected here. Requires an ADMIN bearer token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created; JWT returned"),
            @ApiResponse(responseCode = "400", description = "Validation failed, non-ADMIN role rejected, or email already registered", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Authenticated caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/activate")
    @Operation(tags = {"Authentication"}, summary = "Activate a student account and set a password",
            description = "Public endpoint. A student uses the token from their activation email, together with " +
                    "a password and confirmation, to enable their account. Returns a JWT so they can complete their profile.")
    @SecurityRequirements // public: no bearer lock in Swagger UI
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account activated; JWT returned"),
            @ApiResponse(responseCode = "400", description = "Passwords mismatch, or token invalid/expired", content = @Content)
    })
    public ResponseEntity<AuthResponse> activate(@Valid @RequestBody ActivateAccountRequest request) {
        return ResponseEntity.ok(activationService.activate(request));
    }

    @PostMapping("/resend-activation")
    @Operation(tags = {"Authentication"}, summary = "Resend an activation email",
            description = "Public endpoint. Invalidates the previous activation token, issues a new one, and " +
                    "re-sends the invitation email. Rate-limited by a per-account cooldown.")
    @SecurityRequirements // public
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If the account exists and is not active, a new email was sent"),
            @ApiResponse(responseCode = "400", description = "No pending invitation, or cooldown has not elapsed", content = @Content),
            @ApiResponse(responseCode = "409", description = "Account is already activated", content = @Content),
            @ApiResponse(responseCode = "502", description = "Email could not be sent", content = @Content)
    })
    public ResponseEntity<Void> resendActivation(@Valid @RequestBody ResendActivationRequest request) {
        activationService.resendActivation(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(tags = {"Authentication"}, summary = "Login and receive a JWT token",
            description = "Public endpoint. Exchanges email/password for a Bearer JWT.")
    @SecurityRequirements // public: no bearer lock in Swagger UI
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated; JWT returned"),
            @ApiResponse(responseCode = "400", description = "Missing or invalid email, or missing password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid email or password (generic message)", content = @Content),
            @ApiResponse(responseCode = "403", description = "Account is disabled (not yet activated)", content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(tags = {"Authentication"}, summary = "Get a new access token using a refresh token",
            description = "Public endpoint. Exchanges a valid refresh token for a new access token and a new " +
                    "(rotated) refresh token. The refresh token presented is revoked as soon as it is used.")
    @SecurityRequirements // public: no bearer lock in Swagger UI
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New access and refresh tokens returned"),
            @ApiResponse(responseCode = "400", description = "Missing refresh token", content = @Content),
            @ApiResponse(responseCode = "401", description = "Refresh token is invalid, expired, revoked, or the account is disabled", content = @Content)
    })
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }
}
