package com.shecancode.attendence.registration.service;

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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @InjectMocks
    ProgramService programService;

    @Mock
    ProgramRepository programRepository;

    @Mock
    CohortRepository cohortRepository;

    private Program program;

    private UUID cohortId = UUID.randomUUID();





    @BeforeEach
    void setUp() {

        //SET UP COHORT:

        Cohort cohort = Cohort.builder()
                .id(cohortId)
                .cohortNumber("c10")
                .build();

        program = Program.builder()
                .id(UUID.randomUUID())
                .programName("Backend")
                .cohort(cohort)
                .programRunningPeriod(100)
                .build();

    }

    @Test
    void test_CreateProgram_Successful() {

        when(programRepository.existsById(program.getId())).thenReturn(false);

        when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(program.getCohort()));


        when(programRepository.save(any(Program.class))).thenAnswer
                (invocation ->invocation.getArgument(0));

       assertDoesNotThrow(() -> programService.createProgram(program));

        verify(programRepository,times(1)).save(any(Program.class));
        verify(cohortRepository, times(1)).findById(cohortId);






    }
}