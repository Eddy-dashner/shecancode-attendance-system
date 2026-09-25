package com.shecancode.attendence.registration.Model;

import com.shecancode.attendence.registration.Enum.DisabilityType;
import com.shecancode.attendence.registration.Enum.EducationLevel;
import com.shecancode.attendence.registration.Enum.EnglishProficiency;
import com.shecancode.attendence.registration.Enum.EnglishSkill;
import com.shecancode.attendence.registration.Enum.Gender;
import com.shecancode.attendence.registration.Enum.Occupation;
import com.shecancode.attendence.registration.Enum.ReferralSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Onboarding details a student fills in after activating (mirrors the application
 * portal form). Kept out of {@link Student} so attendance queries stay lean.
 */
@Entity
@Table(name = "student_profile")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "profile_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    // Personal information
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "nationality", nullable = false)
    private String nationality;

    @Column(name = "refugee", nullable = false)
    private boolean refugee;

    // Disability
    @Column(name = "has_disability", nullable = false)
    private boolean hasDisability;

    @Enumerated(EnumType.STRING)
    @Column(name = "disability_type")
    private DisabilityType disabilityType;

    @Column(name = "disability_details", length = 2000)
    private String disabilityDetails;

    // Address
    @Column(name = "province", nullable = false)
    private String province;

    @Column(name = "district", nullable = false)
    private String district;

    @Column(name = "sector", nullable = false)
    private String sector;

    @Column(name = "cell", nullable = false)
    private String cell;

    @Column(name = "village", nullable = false)
    private String village;

    // Emergency contact
    @Column(name = "emergency_contact_name", nullable = false)
    private String emergencyContactName;

    @Column(name = "emergency_contact_relationship", nullable = false)
    private String emergencyContactRelationship;

    @Column(name = "emergency_contact_phone", nullable = false)
    private String emergencyContactPhone;

    // Family & equipment
    @Column(name = "has_young_child", nullable = false)
    private boolean hasYoungChild;

    @Column(name = "has_childcare_support", nullable = false)
    private boolean hasChildcareSupport;

    @Column(name = "has_laptop", nullable = false)
    private boolean hasLaptop;

    // Education & occupation
    @Enumerated(EnumType.STRING)
    @Column(name = "occupation", nullable = false)
    private Occupation occupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = false)
    private EducationLevel educationLevel;

    @Column(name = "institution")
    private String institution;

    @Column(name = "academic_background", nullable = false, length = 2000)
    private String academicBackground;

    @Enumerated(EnumType.STRING)
    @Column(name = "english_proficiency", nullable = false)
    private EnglishProficiency englishProficiency;

    @Enumerated(EnumType.STRING)
    @Column(name = "strongest_english_skill", nullable = false)
    private EnglishSkill strongestEnglishSkill;

    // Additional information
    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "referral_source", nullable = false)
    private ReferralSource referralSource;

    @Column(name = "referral_source_details")
    private String referralSourceDetails;

    @Column(name = "motivation", nullable = false, length = 4000)
    private String motivation;

    @Column(name = "additional_feedback", length = 4000)
    private String additionalFeedback;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
