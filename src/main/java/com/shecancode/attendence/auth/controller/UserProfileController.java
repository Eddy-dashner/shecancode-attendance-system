package com.shecancode.attendence.auth.controller;

import com.shecancode.attendence.auth.dto.UpdateBioRequest;
import com.shecancode.attendence.auth.dto.UserProfileResponse;
import com.shecancode.attendence.auth.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Bio and profile picture for every user")
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/me")
    @Operation(summary = "Get my bio and profile picture",
            description = "Available to every authenticated user (ADMIN, TRAINER, STUDENT).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content)
    })
    public ResponseEntity<UserProfileResponse> getMe(Authentication authentication) {
        return ResponseEntity.ok(profileService.getProfile(authentication.getName()));
    }

    @PatchMapping("/me/bio")
    @Operation(summary = "Update my bio",
            description = "Sets the authenticated user's bio (max 500 characters). A null or blank bio clears it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bio updated"),
            @ApiResponse(responseCode = "400", description = "Bio too long", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content)
    })
    public ResponseEntity<UserProfileResponse> updateBio(Authentication authentication,
                                                         @Valid @RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(profileService.updateBio(authentication.getName(), request.getBio()));
    }

    @PutMapping(value = "/me/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace my profile picture",
            description = "Multipart upload in the `file` part. JPEG, PNG or WEBP, max 5 MB. Stored on Cloudinary " +
                    "and cropped to a 400x400 face-centred square; replaces any previous picture.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture uploaded; profile returned with the new URL"),
            @ApiResponse(responseCode = "400", description = "Missing file, unsupported type, or too large", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "413", description = "Upload exceeds the server's size limit", content = @Content),
            @ApiResponse(responseCode = "502", description = "Image storage provider failed", content = @Content)
    })
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(Authentication authentication,
                                                                    @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(profileService.uploadProfilePicture(authentication.getName(), file));
    }

    @DeleteMapping("/me/profile-picture")
    @Operation(summary = "Remove my profile picture")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Picture removed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content)
    })
    public ResponseEntity<UserProfileResponse> deleteProfilePicture(Authentication authentication) {
        return ResponseEntity.ok(profileService.deleteProfilePicture(authentication.getName()));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get any user's bio and profile picture (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }
}
