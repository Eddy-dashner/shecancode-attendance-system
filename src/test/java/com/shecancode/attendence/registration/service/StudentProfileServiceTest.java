package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Enum.*;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Model.StudentProfile;
import com.shecancode.attendence.registration.Repository.StudentProfileRepository;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    @InjectMocks
    private StudentProfileService profileService;

    @Mock
    private StudentProfileRepository profileRepository;
    @Mock
    private UserRepository userRepository;

    private CompleteProfileRequest request;

    @BeforeEach
    void setUp() {
        request = CompleteProfileRequest.builder()
                .studentFirstName(" Aline ")
                .studentLastName("Keza")
                .dateOfBirth(LocalDate.of(2001, 5, 14))
                .gender(Gender.FEMALE)
                .phoneNumber("0780000001")
                .nationality("Rwandan")
                .province("Kigali City")
                .district("Gasabo")
                .sector("Kimironko")
                .cell("Bibare")
                .village("Ineza")
                .emergencyContactName("Jean Mugabo")
                .emergencyContactRelationship("Parent")
                .emergencyContactPhone("0788000000")
                .occupation(Occupation.INTERNSHIP)
                .educationLevel(EducationLevel.HIGH_SCHOOL)
                .academicBackground("Science stream")
                .englishProficiency(EnglishProficiency.INTERMEDIATE)
                .strongestEnglishSkill(EnglishSkill.READING)
                .linkedinUrl("")
                .referralSource(ReferralSource.FRIENDS)
                .motivation("I want to become a backend developer and build products for my community.")
                .build();
    }

    private Set<String> violations(CompleteProfileRequest r) {
        return VALIDATOR.validate(r).stream()
                .map(ConstraintViolation::getPropertyPath).map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void validRequest_hasNoViolations() {
        assertEquals(Set.of(), violations(request));
    }

    @Test
    void disability_requiresTypeAndDetails() {
        request.setHasDisability(true);
        assertTrue(violations(request).contains("disabilityInfoValid"));

        request.setDisabilityType(DisabilityType.VISUAL_IMPAIRMENT);
        request.setDisabilityDetails("Low vision");
        assertEquals(Set.of(), violations(request));
    }

    @Test
    void universityLevel_requiresInstitution() {
        request.setEducationLevel(EducationLevel.BACHELORS);
        assertTrue(violations(request).contains("institutionValid"));

        request.setInstitution("University of Rwanda");
        assertEquals(Set.of(), violations(request));
    }

    @Test
    void shortMotivationAndBadPhone_areRejected() {
        request.setMotivation("Too short");
        request.setPhoneNumber("+250780000001");
        assertTrue(violations(request).containsAll(Set.of("motivation", "phoneNumber")));
    }

    @Test
    void save_createsProfile_andClearsAnswersWhoseTriggerIsOff() {
        AppUser user = AppUser.builder().username("aline@example.com").build();
        Student student = Student.builder().id(UUID.randomUUID()).user(user).build();
        request.setDisabilityType(DisabilityType.ALBINISM);   // hasDisability is false
        request.setHasChildcareSupport(true);                 // hasYoungChild is false
        request.setInstitution("Some University");            // HIGH_SCHOOL needs none
        when(profileRepository.findByStudent_Id(student.getId())).thenReturn(Optional.empty());
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        StudentProfile profile = profileService.save(student, request);

        assertSame(student, profile.getStudent());
        assertNull(profile.getDisabilityType());
        assertFalse(profile.isHasChildcareSupport());
        assertNull(profile.getInstitution());
        assertNull(profile.getLinkedinUrl());
        assertNotNull(profile.getCompletedAt());
        assertEquals("Aline", student.getStudentFirstName());
        assertEquals("Kimironko, Gasabo, Kigali City", student.getHomeAddress());
        assertEquals("INTERNSHIP", student.getCurrentOccupation());
        assertEquals("Aline Keza", user.getFullName());
    }

    @Test
    void save_updatesExistingProfile_keepingFirstCompletionTime() {
        Student student = Student.builder().id(UUID.randomUUID()).build();
        Instant firstCompleted = Instant.parse("2026-01-01T00:00:00Z");
        StudentProfile existing = StudentProfile.builder().student(student).completedAt(firstCompleted).build();
        when(profileRepository.findByStudent_Id(student.getId())).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        StudentProfile profile = profileService.save(student, request);

        assertSame(existing, profile);
        assertEquals(firstCompleted, profile.getCompletedAt());
        assertTrue(profile.getUpdatedAt().isAfter(firstCompleted));
    }
}
