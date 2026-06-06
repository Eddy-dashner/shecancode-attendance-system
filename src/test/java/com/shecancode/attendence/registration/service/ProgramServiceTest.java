package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
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
class ProgramServiceTest {

    @InjectMocks
    ProgramService programService;

    @Mock
    ProgramRepository programRepository;

    @Mock
    CohortRepository cohortRepository;

    private Program program;


    private final UUID cohortId = UUID.randomUUID();

//    private UUID cohortId = UUID.randomUUID();





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
                .build();

    }

    @Test
    void test_createProgram_Successful() {
        String cohortNumber = "C-123";
        ProgramRequestDao request = new ProgramRequestDao();
        request.setProgramName("Java Backend");
        request.setProgramStartDate(LocalDate.now());
        request.setProgramEndDate(LocalDate.now().plusMonths(6));

        Cohort mockCohort = new Cohort();


        when(cohortRepository.findByCohortNumber(cohortNumber)).thenReturn(Optional.of(mockCohort));

        //  mock: "When you receive any Program, give it back to me"
        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ensure you call the method with the correct parameters!
        ProgramResponseDao result = programService.createProgram(cohortNumber, request);



        assertNotNull(result);
        assertEquals("Java Backend", result.getProgramName());

        // Verify that the repository was actually hit
        verify(cohortRepository, times(1)).findByCohortNumber(cohortNumber);
        verify(programRepository, times(1)).save(any(Program.class));






    }
}