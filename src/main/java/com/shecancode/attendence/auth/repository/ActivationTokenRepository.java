package com.shecancode.attendence.auth.repository;

import com.shecancode.attendence.auth.model.ActivationToken;
import com.shecancode.attendence.auth.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, UUID> {

    Optional<ActivationToken> findByToken(String token);

    List<ActivationToken> findByUserAndUsedAtIsNull(AppUser user);

    Optional<ActivationToken> findFirstByUserOrderByCreatedAtDesc(AppUser user);
}
