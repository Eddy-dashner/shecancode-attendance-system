package com.shecancode.attendence.auth.service;

import com.shecancode.attendence.auth.dto.ActivateAccountRequest;
import com.shecancode.attendence.auth.dto.AuthResponse;
import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.ActivationToken;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.ActivationTokenRepository;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.security.JwtService;
import com.shecancode.attendence.registration.Exception.AccountAlreadyActivatedException;
import com.shecancode.attendence.registration.Exception.ActivationTokenException;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

/**
 * Owns the full account-activation lifecycle: issuing secure one-time tokens,
 * dispatching invitation emails, activating accounts, and resending invitations.
 * All token generation/validation is centralised here so no other component
 * duplicates it.
 */
@Slf4j
@Service
public class ActivationService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final String frontendUrl;
    private final long tokenExpiryHours;
    private final long resendCooldownSeconds;

    public ActivationService(
            UserRepository userRepository,
            ActivationTokenRepository tokenRepository,
            StudentRepository studentRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.activation.token-expiry-hours}") long tokenExpiryHours,
            @Value("${app.activation.resend-cooldown-seconds}") long resendCooldownSeconds
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.studentRepository = studentRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
        this.tokenExpiryHours = tokenExpiryHours;
        this.resendCooldownSeconds = resendCooldownSeconds;
    }

    // ── Invitation dispatch ──────────────────────────────────────────────────

    /** Issues a token and emails a student their enrolment invitation. */
    @Transactional
    public void sendStudentInvitation(AppUser user, Student student) {
        ActivationToken token = issueFreshToken(user);
        emailService.sendStudentInvitation(
                user.getUsername(),
                student.getProgram().getProgramName(),
                student.getCohort().getCohortNumber(),
                buildActivationUrl(token.getToken()),
                token.getExpiresAt());
    }

    /** Issues a token and emails a trainer their invitation. */
    @Transactional
    public void sendTrainerInvitation(AppUser user) {
        ActivationToken token = issueFreshToken(user);
        emailService.sendTrainerInvitation(
                user.getUsername(),
                user.getFullName(),
                buildActivationUrl(token.getToken()),
                token.getExpiresAt());
    }

    // ── Activation ───────────────────────────────────────────────────────────

    /**
     * Validates the token, sets the password, enables the account, advances the
     * account status, and single-uses the token. Returns a JWT.
     */
    @Transactional
    public AuthResponse activate(ActivateAccountRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ActivationTokenException("Password and confirmation do not match.");
        }

        ActivationToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ActivationTokenException("Invalid or already-used activation token."));

        if (token.isUsed()) {
            throw new ActivationTokenException("This activation link has already been used.");
        }
        if (token.isExpired()) {
            throw new ActivationTokenException("This activation link has expired. Please request a new one.");
        }

        AppUser user = token.getUser();
        if (user.isEnabled()) {
            throw new AccountAlreadyActivatedException("This account is already activated.");
        }

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        // Students still have a profile to complete; trainers/admins go straight to active.
        user.setAccountStatus(user.getRole() == Role.STUDENT
                ? AccountStatus.PROFILE_INCOMPLETE
                : AccountStatus.ACTIVE);
        userRepository.save(user);

        // Single-use: mark this token used and invalidate any other outstanding tokens.
        Instant now = Instant.now();
        token.setUsedAt(now);
        invalidateOutstandingTokens(user, now);

        log.info("Account [{}] activated (role {})", user.getUsername(), user.getRole());
        return buildAuthResponse(user);
    }

    // ── Resend ────────────────────────────────────────────────────────────────

    /**
     * Re-issues an activation email for a not-yet-activated account, invalidating
     * the previous token. Guarded by a simple per-account cooldown.
     */
    @Transactional
    public void resendActivation(String email) {
        AppUser user = userRepository.findByUsername(email)
                .orElseThrow(() -> new ActivationTokenException("No pending invitation found for this email."));

        if (user.isEnabled()) {
            throw new AccountAlreadyActivatedException("This account is already activated.");
        }

        tokenRepository.findFirstByUserOrderByCreatedAtDesc(user).ifPresent(last -> {
            long secondsSince = ChronoUnit.SECONDS.between(last.getCreatedAt(), Instant.now());
            if (secondsSince < resendCooldownSeconds) {
                throw new ActivationTokenException(
                        "Please wait before requesting another activation email.");
            }
        });

        if (user.getRole() == Role.STUDENT) {
            Student student = studentRepository.findByEmail(email)
                    .orElseThrow(() -> new ActivationTokenException("No student enrolment found for this email."));
            sendStudentInvitation(user, student);
        } else {
            sendTrainerInvitation(user);
        }
        log.info("Activation email resent to [{}]", email);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ActivationToken issueFreshToken(AppUser user) {
        Instant now = Instant.now();
        invalidateOutstandingTokens(user, now);

        ActivationToken token = ActivationToken.builder()
                .token(generateSecureToken())
                .user(user)
                .createdAt(now)
                .expiresAt(now.plus(tokenExpiryHours, ChronoUnit.HOURS))
                .build();
        return tokenRepository.save(token);
    }

    /** Marks all of a user's still-unused tokens as used, so only the newest is valid. */
    private void invalidateOutstandingTokens(AppUser user, Instant when) {
        List<ActivationToken> outstanding = tokenRepository.findByUserAndUsedAtIsNull(user);
        for (ActivationToken t : outstanding) {
            t.setUsedAt(when);
        }
        tokenRepository.saveAll(outstanding);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildActivationUrl(String token) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/activate?token=" + token;
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
