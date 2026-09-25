package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.registration.Enum.DisabilityType;
import com.shecancode.attendence.registration.Enum.EducationLevel;
import com.shecancode.attendence.registration.Enum.EnglishProficiency;
import com.shecancode.attendence.registration.Enum.EnglishSkill;
import com.shecancode.attendence.registration.Enum.Gender;
import com.shecancode.attendence.registration.Enum.Occupation;
import com.shecancode.attendence.registration.Enum.ReferralSource;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;

/** The onboarding details a student filled in; see {@link CompleteProfileRequest}. */
@Builder
public record StudentProfileResponse(
        LocalDate dateOfBirth,
        Gender gender,
        String nationality,
        boolean refugee,
        boolean hasDisability,
        DisabilityType disabilityType,
        String disabilityDetails,
        String province,
        String district,
        String sector,
        String cell,
        String village,
        String emergencyContactName,
        String emergencyContactRelationship,
        String emergencyContactPhone,
        boolean hasYoungChild,
        boolean hasChildcareSupport,
        boolean hasLaptop,
        Occupation occupation,
        EducationLevel educationLevel,
        String institution,
        String academicBackground,
        EnglishProficiency englishProficiency,
        EnglishSkill strongestEnglishSkill,
        String linkedinUrl,
        String githubUrl,
        ReferralSource referralSource,
        String referralSourceDetails,
        String motivation,
        String additionalFeedback,
        Instant completedAt,
        Instant updatedAt) {}
