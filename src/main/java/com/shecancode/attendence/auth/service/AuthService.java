package com.shecancode.attendence.auth.service;

import com.shecancode.attendence.auth.dto.AuthResponse;
import com.shecancode.attendence.auth.dto.LoginRequest;
import com.shecancode.attendence.auth.dto.RegisterRequest;
import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.security.JwtService;
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

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }

        AppUser user = AppUser.builder()
                .username(request.getUsername())
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        AppUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        log.info("User [{}] logged in successfully", user.getUsername());
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(AppUser user) {
        return AuthResponse.builder()
                .token(jwtService.generateToken(user))
                .tokenType("Bearer")
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .build();
    }
}
