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
import com.shecancode.attendence.registration.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private ActivationTokenRepository tokenRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private EmailService emailService;
    @Mock private JwtService jwtService;

    private ActivationService activationService;

    @BeforeEach
    void setUp() {
        // Real BCrypt-free stand-in encoder to assert hashing happened.
        var encoder = new org.springframework.security.crypto.password.PasswordEncoder() {
            @Override public String encode(CharSequence raw) { return "hashed:" + raw; }
            @Override public boolean matches(CharSequence raw, String enc) { return enc.equals("hashed:" + raw); }
        };
        activationService = new ActivationService(
                userRepository, tokenRepository, studentRepository, emailService,
                encoder, jwtService,
                "http://localhost:5173", 48, 60);
    }

    private ActivationToken tokenFor(AppUser user, Instant expiresAt, Instant usedAt) {
        return ActivationToken.builder()
                .token("tok-123")
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(expiresAt)
                .usedAt(usedAt)
                .build();
    }

    private AppUser invitedStudent() {
        return AppUser.builder()
                .username("aline@example.com")
                .role(Role.STUDENT)
                .enabled(false)
                .accountStatus(AccountStatus.INVITED)
                .build();
    }

    private ActivateAccountRequest req(String token, String pw, String confirm) {
        ActivateAccountRequest r = new ActivateAccountRequest();
        r.setToken(token);
        r.setPassword(pw);
        r.setConfirmPassword(confirm);
        return r;
    }

    @Test
    void activate_validToken_setsPasswordEnablesAndUsesToken() {
        AppUser user = invitedStudent();
        ActivationToken token = tokenFor(user, Instant.now().plus(1, ChronoUnit.HOURS), null);
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.of(token));
        when(tokenRepository.findByUserAndUsedAtIsNull(user)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken(user)).thenReturn("jwt");

        AuthResponse resp = activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!"));

        assertEquals("hashed:Passw0rd!", user.getPassword());
        assertTrue(user.isEnabled());
        assertEquals(AccountStatus.PROFILE_INCOMPLETE, user.getAccountStatus());
        assertNotNull(token.getUsedAt(), "token must be single-used after activation");
        assertEquals("jwt", resp.getToken());
        assertEquals(AccountStatus.PROFILE_INCOMPLETE, resp.getAccountStatus());
        verify(userRepository).save(user);
    }

    @Test
    void activate_trainer_goesStraightToActive() {
        AppUser trainer = AppUser.builder()
                .username("jane@example.com").role(Role.TRAINER)
                .enabled(false).accountStatus(AccountStatus.INVITED).build();
        ActivationToken token = tokenFor(trainer, Instant.now().plus(1, ChronoUnit.HOURS), null);
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.of(token));
        when(tokenRepository.findByUserAndUsedAtIsNull(trainer)).thenReturn(Collections.emptyList());
        when(jwtService.generateToken(trainer)).thenReturn("jwt");

        activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!"));

        assertEquals(AccountStatus.ACTIVE, trainer.getAccountStatus());
    }

    @Test
    void activate_passwordMismatch_throws() {
        assertThrows(ActivationTokenException.class,
                () -> activationService.activate(req("tok-123", "a", "b")));
        verifyNoInteractions(tokenRepository);
    }

    @Test
    void activate_unknownToken_throws() {
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.empty());
        assertThrows(ActivationTokenException.class,
                () -> activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!")));
    }

    @Test
    void activate_expiredToken_throws() {
        AppUser user = invitedStudent();
        ActivationToken token = tokenFor(user, Instant.now().minus(1, ChronoUnit.HOURS), null);
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.of(token));
        assertThrows(ActivationTokenException.class,
                () -> activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!")));
    }

    @Test
    void activate_usedToken_throws() {
        AppUser user = invitedStudent();
        ActivationToken token = tokenFor(user, Instant.now().plus(1, ChronoUnit.HOURS), Instant.now().minusSeconds(5));
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.of(token));
        assertThrows(ActivationTokenException.class,
                () -> activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!")));
    }

    @Test
    void activate_alreadyActivatedAccount_throws() {
        AppUser user = invitedStudent();
        user.setEnabled(true);
        ActivationToken token = tokenFor(user, Instant.now().plus(1, ChronoUnit.HOURS), null);
        when(tokenRepository.findByToken("tok-123")).thenReturn(Optional.of(token));
        assertThrows(AccountAlreadyActivatedException.class,
                () -> activationService.activate(req("tok-123", "Passw0rd!", "Passw0rd!")));
    }

    @Test
    void resend_invalidatesPreviousAndSendsNew_forTrainer() {
        AppUser trainer = AppUser.builder()
                .username("jane@example.com").role(Role.TRAINER)
                .enabled(false).accountStatus(AccountStatus.INVITED).build();
        ActivationToken oldToken = tokenFor(trainer, Instant.now().plus(1, ChronoUnit.HOURS), null);
        oldToken.setCreatedAt(Instant.now().minus(1, ChronoUnit.HOURS)); // past cooldown

        when(userRepository.findByUsername("jane@example.com")).thenReturn(Optional.of(trainer));
        when(tokenRepository.findFirstByUserOrderByCreatedAtDesc(trainer)).thenReturn(Optional.of(oldToken));
        when(tokenRepository.findByUserAndUsedAtIsNull(trainer)).thenReturn(List.of(oldToken));
        when(tokenRepository.save(any(ActivationToken.class))).thenAnswer(i -> i.getArguments()[0]);

        activationService.resendActivation("jane@example.com");

        assertNotNull(oldToken.getUsedAt(), "previous token must be invalidated");
        verify(emailService, times(1)).sendTrainerInvitation(eq("jane@example.com"), any(), anyString(), any());
    }

    @Test
    void resend_alreadyActivated_throws() {
        AppUser trainer = AppUser.builder()
                .username("jane@example.com").role(Role.TRAINER).enabled(true).build();
        when(userRepository.findByUsername("jane@example.com")).thenReturn(Optional.of(trainer));
        assertThrows(AccountAlreadyActivatedException.class,
                () -> activationService.resendActivation("jane@example.com"));
    }
}
