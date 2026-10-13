package com.shecancode.attendence.auth.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.shecancode.attendence.auth.dto.UserProfileResponse;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Exception.ImageUploadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private Cloudinary cloudinary;
    @Mock private Uploader uploader;

    private UserProfileService service;
    private AppUser user;

    @BeforeEach
    void setUp() {
        service = new UserProfileService(userRepository, cloudinary, "test/profile-pictures");
        user = AppUser.builder()
                .id(UUID.randomUUID())
                .username("trainer@shecancode.org")
                .fullName("Jane Trainer")
                .role(Role.TRAINER)
                .build();
        lenient().when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        lenient().when(userRepository.save(any(AppUser.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cloudinary.uploader()).thenReturn(uploader);
    }

    private MockMultipartFile png(int size) {
        return new MockMultipartFile("file", "me.png", "image/png", new byte[size]);
    }

    @Test
    void updateBio_stripsAndBlankClears() {
        assertEquals("Hello", service.updateBio(user.getUsername(), "  Hello  ").getBio());
        assertNull(service.updateBio(user.getUsername(), "   ").getBio());
    }

    @Test
    void upload_storesSecureUrlAndPublicIdKeyedByUserId() throws IOException {
        String publicId = "test/profile-pictures/" + user.getId();
        when(uploader.upload(any(byte[].class), anyMap())).thenReturn(Map.of(
                "secure_url", "https://res.cloudinary.com/x/image/upload/v1/" + publicId + ".png",
                "public_id", publicId));

        UserProfileResponse res = service.uploadProfilePicture(user.getUsername(), png(10));

        assertTrue(res.getProfilePictureUrl().startsWith("https://"));
        assertEquals(publicId, user.getProfilePicturePublicId());
        verify(uploader).upload(any(byte[].class), argThat(opts ->
                user.getId().toString().equals(opts.get("public_id")) && Boolean.TRUE.equals(opts.get("overwrite"))));
    }

    @Test
    void upload_rejectsNonImage() {
        var pdf = new MockMultipartFile("file", "cv.pdf", "application/pdf", new byte[10]);
        assertThrows(IllegalArgumentException.class, () -> service.uploadProfilePicture(user.getUsername(), pdf));
        verifyNoInteractions(uploader);
    }

    @Test
    void upload_rejectsOversizedAndEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> service.uploadProfilePicture(user.getUsername(), png((int) UserProfileService.MAX_PICTURE_BYTES + 1)));
        assertThrows(IllegalArgumentException.class, () -> service.uploadProfilePicture(user.getUsername(), png(0)));
    }

    @Test
    void upload_wrapsProviderFailure() throws IOException {
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("boom"));
        assertThrows(ImageUploadException.class, () -> service.uploadProfilePicture(user.getUsername(), png(10)));
        assertNull(user.getProfilePictureUrl());
    }

    @Test
    void delete_destroysImageAndClearsFields() throws IOException {
        user.setProfilePictureUrl("https://res.cloudinary.com/x.png");
        user.setProfilePicturePublicId("test/profile-pictures/abc");

        UserProfileResponse res = service.deleteProfilePicture(user.getUsername());

        verify(uploader).destroy(eq("test/profile-pictures/abc"), anyMap());
        assertNull(res.getProfilePictureUrl());
        assertNull(user.getProfilePicturePublicId());
    }
}
