package com.shecancode.attendence.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * A long-lived, single-use-then-rotated token that lets a client obtain a new
 * access token without re-authenticating. Kept in its own table (rather than as
 * a self-contained JWT) so it can be looked up and revoked server-side.
 */
@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_token", columnList = "token"),
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

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

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Transient
    public boolean isExpired() {
        return expiresAt == null || Instant.now().isAfter(expiresAt);
    }

    @Transient
    public boolean isRevoked() {
        return revokedAt != null;
    }

    @Transient
    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }
}
