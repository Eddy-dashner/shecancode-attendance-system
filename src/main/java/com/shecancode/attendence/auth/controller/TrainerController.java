package com.shecancode.attendence.auth.controller;

import com.shecancode.attendence.auth.dto.InvitedAccountResponse;
import com.shecancode.attendence.auth.dto.TrainerInviteRequest;
import com.shecancode.attendence.auth.service.TrainerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers", description = "Trainer invitation (ADMIN only)")
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    @Operation(summary = "Invite a trainer (ADMIN only)",
            description = "Creates a disabled TRAINER account and emails an activation link. The trainer sets " +
                    "their own password on activation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Trainer invited; activation email sent"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "409", description = "A user with this email already exists", content = @Content),
            @ApiResponse(responseCode = "502", description = "Invitation email could not be sent", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvitedAccountResponse> inviteTrainer(@Valid @RequestBody TrainerInviteRequest request) {
        return new ResponseEntity<>(trainerService.inviteTrainer(request), HttpStatus.CREATED);
    }
}
