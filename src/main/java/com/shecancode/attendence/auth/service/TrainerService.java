package com.shecancode.attendence.auth.service;

import com.shecancode.attendence.auth.dto.InvitedAccountResponse;
import com.shecancode.attendence.auth.dto.TrainerInviteRequest;
import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Admin-driven creation of TRAINER accounts using the same invitation/activation
 * flow as students: no password is set here — the trainer sets one on activation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerService {

    private final UserRepository userRepository;
    private final ActivationService activationService;

    @Transactional
    public InvitedAccountResponse inviteTrainer(TrainerInviteRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByUsername(email)) {
            throw new EmailAlreadyExistException("A user with this email already exists.");
        }

        AppUser trainer = AppUser.builder()
                .username(email)
                .password(null)
                .fullName(request.getFullName())
                .role(Role.TRAINER)
                .enabled(false)
                .accountStatus(AccountStatus.INVITED)
                .build();
        userRepository.save(trainer);

        activationService.sendTrainerInvitation(trainer);
        log.info("Trainer invited (pending activation): [{}]", email);

        return InvitedAccountResponse.builder()
                .userId(trainer.getId())
                .email(trainer.getUsername())
                .fullName(trainer.getFullName())
                .role(trainer.getRole())
                .accountStatus(trainer.getAccountStatus())
                .build();
    }
}
