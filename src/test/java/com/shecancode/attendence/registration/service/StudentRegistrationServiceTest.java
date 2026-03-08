package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRegistrationServiceTest {

    @InjectMocks
    private StudentRegistrationService registrationService;

    @Mock
    private StudentRepository studentRepository;


    @Mock
    private CohortRepository cohortRepository;


    @Mock
    private ProgramRepository programRepository;


    private StudentRequestDao validRequest;

    private final String cohortNumber = "C10";

    private final  String programName = "Backend";

    @BeforeEach
    void setUp() {
        validRequest = new StudentRequestDao();

        validRequest.setStudentLastName("mupenzi");
        validRequest.setEmail("joseph@gmail.com");
        validRequest.setHomeAddress("123 st");
        validRequest.setCohortNumber(cohortNumber);
        validRequest.setProgramName("Backend");
        validRequest.setCurrentOccupation("Student");
        validRequest.setPhoneNumber("716262722");
        validRequest.setProgramName(programName);
    }


    @Test
    void test_createStudentSuccessfully() {

        //arrange:
        Cohort mockCohort = new Cohort();
        mockCohort.setCohortNumber(cohortNumber);

        Program mockProgram = new Program();
        mockProgram.setProgramName(programName);

        // Given
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(cohortRepository.findByCohortNumber(cohortNumber)).thenReturn(Optional.of(mockCohort));
        when(programRepository.findFirstByProgramName(anyString())).thenReturn(Optional.of(mockProgram));
        when(studentRepository.save(any(Student.class))).thenAnswer(i ->i.getArguments()[0]);

        StudentResponseDao responseDao = registrationService.createStudent(validRequest, cohortNumber);

        assertNotNull(responseDao);
        assertEquals(validRequest.getEmail(), responseDao.getEmail());
        assertEquals("C10",(responseDao.getCohortNumber()));
        verify(studentRepository, times(1)).save(any(Student.class));




    }

    @Test
    void createStudentWith_InvalidEmail_throwsException(){
        validRequest.setEmail("bad-email");

        assertThrows(IllegalArgumentException.class, () ->{
            registrationService.createStudent(validRequest,cohortNumber);
        });
    }
    @Test
    void createStudent_DuplicateEmail_ThrowsException(){
        when(studentRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(EmailAlreadyExistException.class,() ->{
            registrationService.createStudent(validRequest, cohortNumber);
        });

    }

    @Test
    @DisplayName("Error: Should throw Exception when Cohort is not found")
    void createStudent_cohortNotFound_throwsException(){

        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(cohortRepository.findByCohortNumber(cohortNumber)).thenReturn(Optional.empty());

        assertThrows(CohortNotFoundException.class, () ->
                registrationService.createStudent(validRequest,cohortNumber));
    }

    @Test
    void createStudent_Program_NotFound(){
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        Cohort mockCohort = new Cohort();
        when(cohortRepository.findByCohortNumber(anyString())).thenReturn(Optional.of(mockCohort));
        when(programRepository.findFirstByProgramName(programName)).thenReturn(Optional.empty());

        assertThrows(ProgramNotFoundException.class, () -> {
            registrationService.createStudent(validRequest, "C10");
        });
    }

}