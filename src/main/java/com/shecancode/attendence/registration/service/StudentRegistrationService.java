package com.shecancode.attendence.registration.service;

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
import com.shecancode.attendence.registration.Exception.StudentNotFoundException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.AdminCreateStudentRequest;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor // Automatically injects final fields via constructor
public class StudentRegistrationService {

    private final StudentRepository studentRepository;
    private final CohortRepository cohortRepository;
    private final ProgramRepository programsRepository;
    private final UserRepository userRepository;
    private final ActivationService activationService;

    /**
     * Admin enrolment: creates a disabled login account (username = email, role STUDENT,
     * status INVITED) and a PENDING student record linked to it, then emails the student
     * an activation link. The student sets a password via {@code POST /api/v1/auth/activate}
     * and completes their profile afterwards.
     */
    @Transactional
    public StudentResponseDao createStudentAccount(AdminCreateStudentRequest request) {
        String email = request.getEmail();

        // 1. Basic email format validation (defence-in-depth alongside @Email on the DTO)
        if (!isValidEmail(email)) {
            log.error("Enrolment failed: invalid email format [{}]", LoggingUtils.sanitizeForLogging(email));
            throw new IllegalArgumentException("Invalid email format");
        }

        // 2. Prevent duplicate enrolments (student record or login account)
        if (studentRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
            log.warn("Enrolment failed: email [{}] already exists", LoggingUtils.sanitizeForLogging(email));
            throw new EmailAlreadyExistException("A student with this email already exists.");
        }

        // 3. Verify the program exists
        Program program = programsRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ProgramNotFoundException(
                        "Enrolment failed: Program [" + LoggingUtils.sanitizeForLogging(String.valueOf(request.getProgramId())) + "] not found."));

        // 4. Verify the cohort exists
        Cohort cohort = cohortRepository.findById(request.getCohortId())
                .orElseThrow(() -> new CohortNotFoundException(
                        "Enrolment failed: Cohort [" + LoggingUtils.sanitizeForLogging(String.valueOf(request.getCohortId())) + "] not found."));

        // 5. Verify the cohort belongs to the selected program
        if (cohort.getProgram() == null || !cohort.getProgram().getId().equals(program.getId())) {
            throw new CohortProgramMismatchException(
                    "The selected cohort does not belong to the selected program.");
        }

        // 6. Create the disabled, invited login account (no password yet)
        AppUser user = AppUser.builder()
                .username(email)
                .password(null)
                .role(Role.STUDENT)
                .enabled(false)
                .accountStatus(AccountStatus.INVITED)
                .build();
        userRepository.save(user);

        // 7. Create the PENDING student record linked to the account
        Student student = Student.builder()
                .id(UUID.randomUUID())
                .email(email)
                .cohort(cohort)
                .program(program)
                .status(Status.PENDING)
                .user(user)
                .build();
        Student savedStudent = studentRepository.save(student);

        // 8. Issue a token and email the activation link
        activationService.sendStudentInvitation(user, savedStudent);

        log.info("Student invited (pending activation): {} into {} / {}",
                LoggingUtils.sanitizeForLogging(email),
                LoggingUtils.sanitizeForLogging(cohort.getCohortNumber()),
                LoggingUtils.sanitizeForLogging(program.getProgramName()));

        return StudentMapper.toDTO(savedStudent);
    }

    /**
     * Student self-service: fills in the remaining profile fields after activation and
     * moves the record from PENDING to ACTIVE and the account to PROFILE_COMPLETE.
     */
    @Transactional
    public StudentResponseDao completeProfile(String email, CompleteProfileRequest request) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("No student found for [" + LoggingUtils.sanitizeForLogging(email) + "]."));

        student.setStudentFirstName(request.getStudentFirstName());
        student.setStudentLastName(request.getStudentLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setHomeAddress(request.getHomeAddress());
        student.setCurrentOccupation(request.getCurrentOccupation());
        student.setStatus(Status.ACTIVE);

        // Keep the login account's display name in sync and mark onboarding complete.
        AppUser user = student.getUser();
        if (user != null) {
            user.setFullName(student.getFullName());
            user.setAccountStatus(AccountStatus.PROFILE_COMPLETE);
            userRepository.save(user);
        }

        Student savedStudent = studentRepository.save(student);
        log.info("Student [{}] completed their profile", LoggingUtils.sanitizeForLogging(email));

        return StudentMapper.toDTO(savedStudent);
    }

    /**
     * Returns the authenticated student's own profile.
     */
    public StudentResponseDao getMyProfile(String email) {
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new StudentNotFoundException("No student found for [" + LoggingUtils.sanitizeForLogging(email) + "]."));
        return StudentMapper.toDTO(student);
    }

    /**
     * email validation email pattern
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".");
    }


    public List<StudentResponseDao> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentMapper::toDTO)
                .toList();
    }
}
