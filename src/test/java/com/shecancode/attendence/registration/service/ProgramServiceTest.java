package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {

    @InjectMocks
    ProgramService programService;

    @Mock
    ProgramRepository programRepository;

    @Test
    void test_createProgram_Successful() {
        // A program is created on its own; it does not require a cohort.
        ProgramRequestDao request = new ProgramRequestDao();
        request.setProgramName("Java Backend");
        request.setProgramStartDate(LocalDate.now());
        request.setProgramEndDate(LocalDate.now().plusMonths(6));

        when(programRepository.existsByProgramName("Java Backend")).thenReturn(false);
        when(programRepository.save(any(Program.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProgramResponseDao result = programService.createProgram(request);

        assertNotNull(result);
        assertEquals("Java Backend", result.getProgramName());

        verify(programRepository, times(1)).save(any(Program.class));
    }
}