package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import org.junit.jupiter.api.BeforeEach;
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
class CohortServiceTest {

    @Mock
    private CohortRepository cohortRepository;

    @Mock
    private ProgramRepository programRepository;

    @InjectMocks
    private CohortService cohortService;

    private CohortRequestDao cohortRequestToTest;
    private final UUID programId = UUID.randomUUID();
    private Program program;

    @BeforeEach
    void SetUp(){

        program = Program.builder().id(programId).programName("Backend").build();

        cohortRequestToTest = CohortRequestDao.builder()
                .cohortNumber("C10")
                .programId(programId)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

    }

    @Test
    void givenNewCohortNumber_whenCreateCohort_thenCohortIsSaved(){

        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(cohortRepository.findByCohortNumber(cohortRequestToTest.getCohortNumber())).thenReturn(Optional.empty());
        when(cohortRepository.save(any(Cohort.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CohortResponseDao savedCohort = cohortService.createCohort(cohortRequestToTest);

        assertNotNull(savedCohort);
        assertEquals("C10", savedCohort.getCohortNumber());
        assertEquals("Backend", savedCohort.getProgramName());
        verify(cohortRepository).findByCohortNumber("C10");
        verify(cohortRepository, times(1)).save(any(Cohort.class));
    }

    @Test
    void test_createCohort_AlreadyExists_ThrowsException(){
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(cohortRepository.findByCohortNumber(cohortRequestToTest.getCohortNumber())).thenReturn(Optional.of(new Cohort()));

        assertThrows(CohortAlreadyExistException.class, () -> cohortService.createCohort(cohortRequestToTest));
    }

    @Test
    void test_createCohort_InvalidProgram_ThrowsNotFound(){
        when(programRepository.findById(programId)).thenReturn(Optional.empty());

        assertThrows(ProgramNotFoundException.class, () -> cohortService.createCohort(cohortRequestToTest));
        verify(cohortRepository, never()).save(any(Cohort.class));
    }


}