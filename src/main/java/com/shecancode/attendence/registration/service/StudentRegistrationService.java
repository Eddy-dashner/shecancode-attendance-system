package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
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
    public StudentResponseDao createStudent(StudentRequestDao studentRequestDao, String cohortNumber){
        if ( studentRepository.existsByEmail(studentRequestDao.getEmail())){
            log.error("Student with this email exist");

            throw new EmailAlreadyExistException(" A student with this email exists");
        }


        Cohort createdCohort = cohortRepository.findByCohortNumber(cohortNumber)
                .orElseThrow(()-> new CohortNotFoundException("cohort not found"));

        // Create student with the saved cohort
        Student newStudent = StudentMapper.toModelStudent(studentRequestDao);
        newStudent.setCohort(createdCohort);
        newStudent = studentRepository.save(newStudent);

        return StudentMapper.toDTO(newStudent);
    }
}
