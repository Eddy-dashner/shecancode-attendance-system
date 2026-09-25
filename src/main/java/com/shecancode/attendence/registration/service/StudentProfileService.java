package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Model.StudentProfile;
import com.shecancode.attendence.registration.Repository.StudentProfileRepository;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Writes the onboarding profile, shared by the student's own completion/edit and the
 * admin edit. Enrolment status and account status are left to the callers.
 */
@Service
@RequiredArgsConstructor
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    /** Copies the request onto the student row and creates or updates their profile. */
    @Transactional
    public StudentProfile save(Student student, CompleteProfileRequest request) {
        student.setStudentFirstName(request.getStudentFirstName().trim());
        student.setStudentLastName(request.getStudentLastName().trim());
        student.setPhoneNumber(request.getPhoneNumber());
        // Summary copies on the student row, so lists can show them without loading the profile.
        student.setHomeAddress(String.join(", ",
                request.getSector().trim(), request.getDistrict().trim(), request.getProvince().trim()));
        student.setCurrentOccupation(request.getOccupation().name());

        // Keep the login account's display name in sync.
        AppUser user = student.getUser();
        if (user != null) {
            user.setFullName(student.getFullName());
            userRepository.save(user);
        }

        Instant now = Instant.now();
        StudentProfile profile = profileRepository.findByStudent_Id(student.getId())
                .orElseGet(() -> StudentProfile.builder().student(student).completedAt(now).build());
        applyProfile(profile, request);
        profile.setUpdatedAt(now);
        return profileRepository.save(profile);
    }

    // Conditional answers are cleared when their trigger is off, so stale values never linger.
    private static void applyProfile(StudentProfile p, CompleteProfileRequest r) {
        p.setDateOfBirth(r.getDateOfBirth());
        p.setGender(r.getGender());
        p.setNationality(r.getNationality().trim());
        p.setRefugee(r.isRefugee());

        p.setHasDisability(r.isHasDisability());
        p.setDisabilityType(r.isHasDisability() ? r.getDisabilityType() : null);
        p.setDisabilityDetails(r.isHasDisability() ? trimToNull(r.getDisabilityDetails()) : null);

        p.setProvince(r.getProvince().trim());
        p.setDistrict(r.getDistrict().trim());
        p.setSector(r.getSector().trim());
        p.setCell(r.getCell().trim());
        p.setVillage(r.getVillage().trim());

        p.setEmergencyContactName(r.getEmergencyContactName().trim());
        p.setEmergencyContactRelationship(r.getEmergencyContactRelationship().trim());
        p.setEmergencyContactPhone(r.getEmergencyContactPhone());

        p.setHasYoungChild(r.isHasYoungChild());
        p.setHasChildcareSupport(r.isHasYoungChild() && r.isHasChildcareSupport());
        p.setHasLaptop(r.isHasLaptop());

        p.setOccupation(r.getOccupation());
        p.setEducationLevel(r.getEducationLevel());
        p.setInstitution(r.getEducationLevel().requiresInstitution() ? trimToNull(r.getInstitution()) : null);
        p.setAcademicBackground(r.getAcademicBackground().trim());
        p.setEnglishProficiency(r.getEnglishProficiency());
        p.setStrongestEnglishSkill(r.getStrongestEnglishSkill());

        p.setLinkedinUrl(trimToNull(r.getLinkedinUrl()));
        p.setGithubUrl(trimToNull(r.getGithubUrl()));
        p.setReferralSource(r.getReferralSource());
        p.setReferralSourceDetails(trimToNull(r.getReferralSourceDetails()));
        p.setMotivation(r.getMotivation().trim());
        p.setAdditionalFeedback(trimToNull(r.getAdditionalFeedback()));
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
