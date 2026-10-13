package com.shecancode.attendence.auth.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.shecancode.attendence.auth.dto.UserProfileResponse;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Exception.ImageUploadException;
import com.shecancode.attendence.registration.Exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Bio and profile picture for any user (ADMIN, TRAINER or STUDENT). Pictures are
 * stored in Cloudinary under a stable public_id per user, so a re-upload
 * overwrites the previous image instead of leaving orphans behind.
 */
@Slf4j
@Service
public class UserProfileService {

    static final long MAX_PICTURE_BYTES = 5 * 1024 * 1024;
    static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final Cloudinary cloudinary;
    private final String folder;

    public UserProfileService(UserRepository userRepository,
                              Cloudinary cloudinary,
                              @Value("${app.cloudinary.profile-picture-folder}") String folder) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
        this.folder = folder;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String username) {
        return toResponse(findByUsername(username));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID userId) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateBio(String username, String bio) {
        AppUser user = findByUsername(username);
        user.setBio(bio == null || bio.isBlank() ? null : bio.strip());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse uploadProfilePicture(String username, MultipartFile file) {
        validate(file);
        AppUser user = findByUsername(username);

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "public_id", user.getId().toString(),
                    "overwrite", true,
                    "invalidate", true,
                    "resource_type", "image",
                    // Normalise to a square avatar on upload so clients need no transforms.
                    "transformation", "c_fill,g_face,w_400,h_400,q_auto,f_auto"));
        } catch (IOException | RuntimeException ex) {
            log.error("Profile picture upload failed for user [{}]", user.getId(), ex);
            throw new ImageUploadException("Profile picture upload failed.", ex);
        }

        user.setProfilePictureUrl((String) result.get("secure_url"));
        user.setProfilePicturePublicId((String) result.get("public_id"));
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse deleteProfilePicture(String username) {
        AppUser user = findByUsername(username);
        String publicId = user.getProfilePicturePublicId();
        if (publicId != null) {
            try {
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));
            } catch (IOException | RuntimeException ex) {
                // The DB reference is still cleared; an orphaned image is harmless.
                log.warn("Could not delete Cloudinary image [{}]", publicId, ex);
            }
        }
        user.setProfilePictureUrl(null);
        user.setProfilePicturePublicId(null);
        return toResponse(userRepository.save(user));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A non-empty image file is required.");
        }
        if (file.getSize() > MAX_PICTURE_BYTES) {
            throw new IllegalArgumentException("Profile picture must be at most 5 MB.");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Profile picture must be a JPEG, PNG or WEBP image.");
        }
    }

    private AppUser findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }

    private UserProfileResponse toResponse(AppUser user) {
        return UserProfileResponse.builder()
                .userId(user.getId())
                .email(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole())
                .accountStatus(user.getAccountStatus())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}
