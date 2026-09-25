package com.shecancode.attendence.registration.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shecancode.attendence.registration.Enum.DisabilityType;
import com.shecancode.attendence.registration.Enum.EducationLevel;
import com.shecancode.attendence.registration.Enum.EnglishProficiency;
import com.shecancode.attendence.registration.Enum.EnglishSkill;
import com.shecancode.attendence.registration.Enum.Gender;
import com.shecancode.attendence.registration.Enum.Occupation;
import com.shecancode.attendence.registration.Enum.ReferralSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

/**
 * Student-facing payload to complete (or later update) their profile after activating
 * the account. Mirrors the application portal form. Email, cohort and program are
 * already set by the admin and are not editable here.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Student profile completion payload")
public class CompleteProfileRequest {

    // ---- Personal information ----

    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50)
    @Schema(example = "Aline")
    private String studentFirstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 2, max = 50)
    @Schema(example = "Keza")
    private String studentLastName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Schema(example = "2001-05-14")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    @Schema(example = "0780000001")
    private String phoneNumber;

    @NotBlank(message = "Nationality is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Rwandan")
    private String nationality;

    @Schema(description = "Defaults to false")
    private boolean refugee;

    // ---- Disability ----

    @Schema(description = "Defaults to false")
    private boolean hasDisability;

    @Schema(description = "Required when hasDisability is true")
    private DisabilityType disabilityType;

    @Size(max = 2000)
    @Schema(description = "Required when hasDisability is true")
    private String disabilityDetails;

    // ---- Address ----

    @NotBlank(message = "Province is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Kigali City")
    private String province;

    @NotBlank(message = "District is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Gasabo")
    private String district;

    @NotBlank(message = "Sector is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Kimironko")
    private String sector;

    @NotBlank(message = "Cell is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Bibare")
    private String cell;

    @NotBlank(message = "Village is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Ineza")
    private String village;

    // ---- Emergency contact ----

    @NotBlank(message = "Emergency contact name is required")
    @Size(min = 2, max = 100)
    @Schema(example = "Jean Mugabo")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact relationship is required")
    @Size(min = 2, max = 50)
    @Schema(example = "Parent")
    private String emergencyContactRelationship;

    @NotBlank(message = "Emergency contact phone is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Emergency contact phone must be 10 to 15 digits")
    @Schema(example = "0788000000")
    private String emergencyContactPhone;

    // ---- Family & equipment ----

    @Schema(description = "Defaults to false")
    private boolean hasYoungChild;

    @Schema(description = "Only meaningful when hasYoungChild is true; ignored otherwise")
    private boolean hasChildcareSupport;

    @Schema(description = "Defaults to false")
    private boolean hasLaptop;

    // ---- Education & occupation ----

    @NotNull(message = "Current occupation is required")
    private Occupation occupation;

    @NotNull(message = "Education level is required")
    private EducationLevel educationLevel;

    @Size(max = 255)
    @Schema(description = "Required for university-level education (year 1-4, final year, bachelors, masters, PhD)",
            example = "University of Rwanda")
    private String institution;

    @NotBlank(message = "Academic background is required")
    @Size(min = 2, max = 2000)
    private String academicBackground;

    @NotNull(message = "English proficiency is required")
    private EnglishProficiency englishProficiency;

    @NotNull(message = "Most confident English skill is required")
    private EnglishSkill strongestEnglishSkill;

    // ---- Additional information ----

    @URL(message = "LinkedIn profile must be a valid URL")
    @Size(max = 500)
    @Schema(example = "https://www.linkedin.com/in/aline-keza")
    private String linkedinUrl;

    @URL(message = "GitHub profile must be a valid URL")
    @Size(max = 500)
    @Schema(example = "https://github.com/alinekeza")
    private String githubUrl;

    @NotNull(message = "Please tell us how you heard about us")
    private ReferralSource referralSource;

    @Size(max = 255)
    @Schema(description = "Optional detail for referralSource (e.g. ambassador name, school)")
    private String referralSourceDetails;

    @NotBlank(message = "Motivation is required")
    @Size(min = 50, max = 4000, message = "Motivation must be between 50 and 4000 characters")
    private String motivation;

    @Size(max = 4000)
    private String additionalFeedback;

    // ---- Conditional rules (reported as field errors named after these getters) ----

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "Disability type and details are required when you have a disability")
    public boolean isDisabilityInfoValid() {
        return !hasDisability || (disabilityType != null && disabilityDetails != null && !disabilityDetails.isBlank());
    }

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "University / institution is required for university-level education")
    public boolean isInstitutionValid() {
        return educationLevel == null || !educationLevel.requiresInstitution()
                || (institution != null && institution.trim().length() >= 2);
    }
}
