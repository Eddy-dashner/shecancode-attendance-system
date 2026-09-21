package com.shecancode.attendence.auth.service;

import com.shecancode.attendence.auth.dto.AuthResponse;
import com.shecancode.attendence.auth.dto.LoginRequest;
import com.shecancode.attendence.auth.dto.RegisterRequest;
import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.RefreshToken;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.security.JwtService;
import com.shecancode.attendence.auth.util.EmailUtils;
import com.shecancode.attendence.registration.Exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;

    /**
     * Password-based creation of an ADMIN account (bootstrap / adding more admins).
     * TRAINER and STUDENT accounts are created via the invitation flow instead
     * (see {@code TrainerController} and the student enrolment endpoint), so they
     * are rejected here.
     */
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Only ADMIN accounts can be created here. Invite trainers and students via their invitation endpoints.");
        }

        String email = EmailUtils.normalize(request.getEmail());
        if (userRepository.existsByUsername(email)) {
            throw new IllegalArgumentException(
                    "An account with email '" + email + "' already exists.");
        }

        AppUser user = AppUser.builder()
                .username(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(request.getRole())
                .enabled(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        userRepository.save(user);
        log.info("Registered new user [{}] with role [{}]", user.getUsername(), user.getRole());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = EmailUtils.normalize(request.getEmail());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        AppUser user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        log.info("User [{}] logged in successfully", user.getUsername());
        return buildAuthResponse(user);
    }

    /**
     * Exchanges a valid, unexpired, unrevoked refresh token for a new access
     * token. The refresh token is rotated: the one presented is revoked and a
     * new one is returned alongside the new access token.
     */
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken rotated = refreshTokenService.rotate(rawRefreshToken);
        AppUser user = rotated.getUser();

        if (!user.isEnabled()) {
            throw new InvalidRefreshTokenException("Account is disabled.");
        }

        log.info("Refreshed access token for user [{}]", user.getUsername());
        return buildAuthResponse(user, rotated.getToken());
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        return buildAuthResponse(user, refreshTokenService.issue(user).getToken());
    }

    private AuthResponse buildAuthResponse(AppUser user, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(jwtService.generateAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .build();
    }
}
