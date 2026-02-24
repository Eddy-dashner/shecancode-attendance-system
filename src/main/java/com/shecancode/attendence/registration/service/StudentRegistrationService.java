package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor // Automatically injects final fields via constructor
public class StudentRegistrationService {

    private final StudentRepository studentRepository;
    private final CohortRepository cohortRepository;
    private final ProgramRepository programsRepository;

    /**
     * Registers a new student to a specific cohort and program.
     * @param studentRequestDao The student details from JSON body
     * @param cohortNumber The cohort identifier from the URL path
     */
    @Transactional
    public StudentResponseDao createStudent(StudentRequestDao studentRequestDao, String cohortNumber) {

        // 1. Basic Email Format Validation
        if (!isValidEmail(studentRequestDao.getEmail())) {
            log.error("Registration failed: Invalid email format [{}]", studentRequestDao.getEmail());
            throw new IllegalArgumentException("Invalid email format");
        }

        // 2. Prevent Duplicate Registrations
        if (studentRepository.existsByEmail(studentRequestDao.getEmail())) {
            log.warn("Registration failed: Email [{}] already exists", studentRequestDao.getEmail());
            throw new EmailAlreadyExistException("A student with this email already exists.");
        }

        // 3. Lookup Cohort (Source: URL Path Variable)
        Cohort cohort = cohortRepository.findByCohortNumber(cohortNumber)
                .orElseThrow(() -> new CohortNotFoundException("Registration failed: Cohort [" + cohortNumber + "] not found."));

        // 4. Lookup Program (Source: JSON Body)
        Program program = programsRepository.findByProgramName(studentRequestDao.getProgramName())
                .orElseThrow(() -> new ProgramNotFoundException("Registration failed: Program [" + studentRequestDao.getProgramName() + "] not found."));

        // 5. Build Student Entity with Relationships
        Student student = Student.builder()
                .studentFirstName(studentRequestDao.getStudentFirstName())
                .studentLastName(studentRequestDao.getStudentLastName())
                .email(studentRequestDao.getEmail())
                .phoneNumber(studentRequestDao.getPhoneNumber())
                .homeAddress(studentRequestDao.getHomeAddress())
                .currentOccupation(studentRequestDao.getCurrentOccupation())
                .cohort(cohort)   // Links to existing Cohort entry
                .program(program) // Links to existing Program entry
                .status(Status.ACTIVE)
                .build();

        // 6. Persist to Database
        Student savedStudent = studentRepository.save(student);
        log.info("Student successfully registered: {} assigned to {} in {}",
                savedStudent.getEmail(), cohort.getCohortNumber(), program.getProgramName());

        // 7. Convert to Response DTO
        return StudentMapper.toDTO(savedStudent);
    }

    /**
     * email validation email pattern
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return false;
        return email.contains("@") && email.contains(".") &&
                email.indexOf("@") < email.lastIndexOf(".");
    }
}