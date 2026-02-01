package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Programs;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramsRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class StudentRegistrationService {

    private final StudentRepository studentRepository;
    private final CohortRepository cohortRepository;
    private final ProgramsRepository programsRepository;

    public StudentRegistrationService(StudentRepository studentRepository, 
                                    CohortRepository cohortRepository,
                                    ProgramsRepository programsRepository) {
        this.studentRepository = studentRepository;
        this.cohortRepository = cohortRepository;
        this.programsRepository = programsRepository;
    }
    @Transactional
    public StudentResponseDao createStudent(StudentRequestDao studentRequestDao){
        if ( studentRepository.existsByEmail(studentRequestDao.getEmail())){
            log.error("Student with this email exist");

            throw new EmailAlreadyExistException(" A student with this email exists");
        }

        // Use the provided cohort or create default if not provided
        Cohort cohort;
        if (studentRequestDao.getCohortId() != null) {
            cohort = cohortRepository.findById(studentRequestDao.getCohortId())
                    .orElseThrow(() -> new RuntimeException("Cohort not found with ID: " + studentRequestDao.getCohortId()));
        } else {
            // Find or create a default cohort
            cohort = cohortRepository.findByCohortNumber("DEFAULT-COHORT")
                    .orElseGet(() -> {
                        Cohort newCohort = new Cohort();
                        newCohort.setCohortNumber("DEFAULT-COHORT");
                        newCohort.setStartDate(java.time.LocalDate.now());
                        newCohort.setEndDate(java.time.LocalDate.now().plusMonths(6));
                        newCohort.setGraduationDate(java.time.LocalDate.now().plusMonths(6));
                        // Create a default program
                        Programs defaultProgram = Programs.builder()
                                .id(UUID.randomUUID())
                                .programName("DEFAULT-PROGRAM")
                                .requiredProgramDuration(88)
                                .build();
                        defaultProgram = programsRepository.save(defaultProgram);
                        newCohort.setProgram(defaultProgram);
                        return cohortRepository.save(newCohort);
                    });
        }

        // Create student with the saved cohort
        Student newStudent = StudentMapper.toModelStudent(studentRequestDao, cohort);
        newStudent = studentRepository.save(newStudent);

        return StudentMapper.toDTO(newStudent);
    }
}
