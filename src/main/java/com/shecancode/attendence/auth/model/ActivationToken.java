package com.shecancode.attendence.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A single-use, expiring account-activation token issued when an admin invites a
 * user. Kept in its own table so a user can have a history of tokens (e.g. after a
 * resend) while only the latest unused, unexpired one is valid.
 */
@Entity
@Table(name = "activation_token", indexes = {
        @Index(name = "idx_activation_token_token", columnList = "token"),
        @Index(name = "idx_activation_token_user", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Transient
    public boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    @Transient
    public boolean isUsed() {
        return usedAt != null;
    }

    @Transient
    public boolean isValid() {
        return !isUsed() && !isExpired();
    }
}
