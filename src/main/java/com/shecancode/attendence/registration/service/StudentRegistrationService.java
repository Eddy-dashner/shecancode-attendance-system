package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.auth.util.EmailUtils;
import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.service.ActivationService;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Exception.CohortProgramMismatchException;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Exception.ReadOnlyException;
import com.shecancode.attendence.registration.Exception.StudentNotFoundException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Model.StudentProfile;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentProfileRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.AdminCreateStudentRequest;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentRegistrationService {

    private final StudentRepository studentRepository;
    private final CohortRepository cohortRepository;
    private final ProgramRepository programsRepository;
    private final UserRepository userRepository;
    private final ActivationService activationService;
    private final StudentProfileRepository profileRepository;
    private final StudentProfileService profileService;

    /**
     * Admin enrolment: creates a disabled login account (username = email, role STUDENT,
     * status INVITED) and a PENDING student record linked to it, then emails the student
     * an activation link. The student sets a password via {@code POST /api/v1/auth/activate}
     * and completes their profile afterwards.
     */
    @Transactional
    public StudentResponseDao createStudentAccount(AdminCreateStudentRequest request) {
        String email = EmailUtils.normalize(request.getEmail());

        // Defence-in-depth alongside @Email on the DTO.
        if (!isValidEmail(email)) {
            log.error("Enrolment failed: invalid email format [{}]", LoggingUtils.sanitizeForLogging(email));
            throw new IllegalArgumentException("Invalid email format");
        }

        if (studentRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
            log.warn("Enrolment failed: email [{}] already exists", LoggingUtils.sanitizeForLogging(email));
            throw new EmailAlreadyExistException("A student with this email already exists.");
        }

        Program program = programsRepository.findByIdAndDeletedAtIsNull(request.getProgramId())
                .orElseThrow(() -> new ProgramNotFoundException(
                        "Enrolment failed: Program [" + LoggingUtils.sanitizeForLogging(String.valueOf(request.getProgramId())) + "] not found."));

        Cohort cohort = cohortRepository.findByIdAndDeletedAtIsNull(request.getCohortId())
                .orElseThrow(() -> new CohortNotFoundException(
                        "Enrolment failed: Cohort [" + LoggingUtils.sanitizeForLogging(String.valueOf(request.getCohortId())) + "] not found."));

        if (cohort.getProgram() == null || !cohort.getProgram().getId().equals(program.getId())) {
            throw new CohortProgramMismatchException(
                    "The selected cohort does not belong to the selected program.");
        }

        if (cohort.isReadOnly()) {
            throw new ReadOnlyException("Cohort " + LoggingUtils.sanitizeForLogging(cohort.getCohortNumber())
                    + " is closed; reopen it to add students.");
        }

        AppUser user = AppUser.builder()
                .username(email)
                .password(null)
                .role(Role.STUDENT)
                .enabled(false)
                .accountStatus(AccountStatus.INVITED)
                .build();
        userRepository.save(user);

        Student student = Student.builder()
                .id(UUID.randomUUID())
                .email(email)
                .cohort(cohort)
                .program(program)
                .status(Status.PENDING)
                .user(user)
                .build();
        Student savedStudent = studentRepository.save(student);

        activationService.sendStudentInvitation(user, savedStudent);

        log.info("Student invited (pending activation): {} into {} / {}",
                LoggingUtils.sanitizeForLogging(email),
                LoggingUtils.sanitizeForLogging(cohort.getCohortNumber()),
                LoggingUtils.sanitizeForLogging(program.getProgramName()));

        return StudentMapper.toDTO(savedStudent);
    }

    /**
     * Student self-service: fills in (or later updates) the onboarding profile. The first
     * completion moves the record from PENDING to ACTIVE and the account to PROFILE_COMPLETE;
     * later edits leave the enrolment status alone (so a dropped-out student stays dropped out).
     */
    @Transactional
    public StudentResponseDao completeProfile(String email, CompleteProfileRequest request) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("No student found for [" + LoggingUtils.sanitizeForLogging(email) + "]."));

        StudentProfile profile = profileService.save(student, request);
        if (student.getStatus() == Status.PENDING) {
            student.setStatus(Status.ACTIVE);
        }

        // Mark onboarding complete on the login account.
        AppUser user = student.getUser();
        if (user != null) {
            user.setAccountStatus(AccountStatus.PROFILE_COMPLETE);
            userRepository.save(user);
        }

        log.info("Student [{}] saved their profile", LoggingUtils.sanitizeForLogging(email));
        return StudentMapper.toDTO(studentRepository.save(student), profile);
    }

    @Transactional(readOnly = true)
    public StudentResponseDao getMyProfile(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("No student found for [" + LoggingUtils.sanitizeForLogging(email) + "]."));
        return StudentMapper.toDTO(student, profileRepository.findByStudent_Id(student.getId()).orElse(null));
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".");
    }
}
