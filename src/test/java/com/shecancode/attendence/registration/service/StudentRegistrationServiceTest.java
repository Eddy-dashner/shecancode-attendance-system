package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.auth.model.AccountStatus;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.service.ActivationService;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Exception.CohortProgramMismatchException;
import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.AdminCreateStudentRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private ProgramRepository programsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ActivationService activationService;

    private final UUID programId = UUID.randomUUID();
    private final UUID cohortId = UUID.randomUUID();

    private AdminCreateStudentRequest validRequest;
    private Cohort cohort;
    private Program program;

    @BeforeEach
    void setUp() {
        validRequest = AdminCreateStudentRequest.builder()
                .email("joseph@gmail.com")
                .programId(programId)
                .cohortId(cohortId)
                .build();

        program = Program.builder().id(programId).programName("Backend").build();
        cohort = Cohort.builder().id(cohortId).cohortNumber("Cohort-10").program(program).build();
    }

    @Test
    void createStudentAccount_success_invitesAndReturnsPendingStudent() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(programsRepository.findById(programId)).thenReturn(Optional.of(program));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort));
        when(studentRepository.save(any(Student.class))).thenAnswer(i -> i.getArguments()[0]);

        StudentResponseDao response = registrationService.createStudentAccount(validRequest);

        assertNotNull(response);
        assertEquals("joseph@gmail.com", response.getEmail());
        assertEquals("Cohort-10", response.getCohortNumber());

        // A disabled, INVITED student account with no password is created
        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());
        AppUser createdUser = userCaptor.getValue();
        assertFalse(createdUser.isEnabled());
        assertNull(createdUser.getPassword());
        assertEquals(Role.STUDENT, createdUser.getRole());
        assertEquals(AccountStatus.INVITED, createdUser.getAccountStatus());

        verify(activationService, times(1)).sendStudentInvitation(any(AppUser.class), any(Student.class));
    }

    @Test
    void createStudentAccount_invalidEmail_throws() {
        validRequest.setEmail("bad-email");
        assertThrows(IllegalArgumentException.class,
                () -> registrationService.createStudentAccount(validRequest));
        verify(activationService, never()).sendStudentInvitation(any(), any());
    }

    @Test
    void createStudentAccount_duplicateEmail_throws() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(true);
        assertThrows(EmailAlreadyExistException.class,
                () -> registrationService.createStudentAccount(validRequest));
    }

    @Test
    void createStudentAccount_programNotFound_throws() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(programsRepository.findById(programId)).thenReturn(Optional.empty());
        assertThrows(ProgramNotFoundException.class,
                () -> registrationService.createStudentAccount(validRequest));
    }

    @Test
    void createStudentAccount_cohortNotFound_throws() {
        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(programsRepository.findById(programId)).thenReturn(Optional.of(program));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.empty());
        assertThrows(CohortNotFoundException.class,
                () -> registrationService.createStudentAccount(validRequest));
    }

    @Test
    @DisplayName("Cohort must belong to the selected program")
    void createStudentAccount_cohortNotInProgram_throws() {
        // The requested cohort belongs to a different program than the requested one.
        Program otherProgram = Program.builder().id(UUID.randomUUID()).programName("Frontend").build();
        Cohort mismatchedCohort = Cohort.builder().id(cohortId).cohortNumber("Cohort-10").program(otherProgram).build();

        when(studentRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(programsRepository.findById(programId)).thenReturn(Optional.of(program));
        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(mismatchedCohort));

        assertThrows(CohortProgramMismatchException.class,
                () -> registrationService.createStudentAccount(validRequest));
        verify(activationService, never()).sendStudentInvitation(any(), any());
    }
}
