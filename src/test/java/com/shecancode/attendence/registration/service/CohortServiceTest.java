package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CohortServiceTest {

    @Mock
    private CohortRepository cohortRepository;

    @InjectMocks
    private CohortService cohortService;

    private CohortRequestDao cohortRequestToTest;
    private final String program = "Backend";

    @BeforeEach
    void SetUp(){

        cohortRequestToTest = CohortRequestDao.builder()
                .cohortNumber("C10")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now())
                .build();

    }

    @Test
    void givenNewCohortNumber_whenCreateCohort_thenCohortIsSaved(){

        when(cohortRepository.findByCohortNumber(cohortRequestToTest.getCohortNumber())).thenReturn(Optional.empty());
        when(cohortRepository.save(any(Cohort.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CohortResponseDao savedCohort = cohortService.createCohort(cohortRequestToTest);

        assertNotNull(savedCohort);
        assertEquals("C10", savedCohort.getCohortNumber());
        verify(cohortRepository).findByCohortNumber("C10");
        verify(cohortRepository, times(1)).save(any(Cohort.class));
    }

    @Test
    void test_createCohort_AlreadyExists_ThrowsException(){
        when(cohortRepository.findByCohortNumber(cohortRequestToTest.getCohortNumber())).thenReturn(Optional.of(new Cohort()));

        assertThrows(CohortAlreadyExistException.class, () -> cohortService.createCohort(cohortRequestToTest));
    }


}