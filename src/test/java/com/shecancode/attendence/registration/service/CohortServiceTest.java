package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CohortServiceTest {

    @Mock
    private CohortRepository cohortRepository;

    @InjectMocks
    private CohortService cohortService;

    private Cohort cohortToTest;
    private final String program = "Backend";

    @BeforeEach
    void SetUp(){

        cohortToTest = new Cohort();
        cohortToTest.setCohortNumber("C10");
        cohortToTest.setStartDate(LocalDate.now());
        cohortToTest.setEndDate(LocalDate.now());
        cohortToTest.setGraduationDate(LocalDate.now());
        cohortToTest.setPrograms(new ArrayList<>());

    }

    @Test
    void givenNewCohortNumber_whenCreateCohort_thenCohortIsSaved(){

        when(cohortRepository.findByCohortNumber(cohortToTest.getCohortNumber())).thenReturn(Optional.empty());
        when(cohortRepository.save(any(Cohort.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cohort savedCohort = cohortService.createCohort(cohortToTest);

        assertNotNull(savedCohort);
        assertEquals("C10", savedCohort.getCohortNumber());
        assertTrue(savedCohort.getPrograms().isEmpty());
        verify(cohortRepository).findByCohortNumber("C10");
        verify(cohortRepository, times(1)).save(any(Cohort.class));
    }

    @Test
    void test_createCohort_AlreadyExists_ThrowsException(){
        when(cohortRepository.findByCohortNumber(cohortToTest.getCohortNumber())).thenReturn(Optional.of(cohortToTest));

        assertThrows(CohortAlreadyExistException.class, () -> cohortService.createCohort(cohortToTest));
    }


}