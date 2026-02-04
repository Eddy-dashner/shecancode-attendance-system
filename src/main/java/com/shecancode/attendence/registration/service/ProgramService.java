package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ProgramService {
    private final ProgramRepository programRepository;

    private CohortRepository cohortRepository;

    public ProgramService(ProgramRepository programRepository, CohortRepository cohortRepository) {
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
    }

    public void createProgram(Program program){
        if (program.getId() != null && programRepository.existsById(program.getId())) {
            throw new IllegalArgumentException("Program already exists with ID: " + program.getId());
        }
        if (program.getCohort() == null || program.getCohort().getId() == null){
            log.error("Cohort reference is missing in the request.");
            throw new CohortNotFoundException("Cohort not found");
        }

        cohortRepository.findById(program.getCohort().getId())
                .orElseThrow(() -> new CohortNotFoundException("Cohort not found in database"));

        Program newProgram = Program.builder()
                .id(UUID.randomUUID())
                .programName(program.getProgramName())
                .cohort(program.getCohort())
                .programRunningPeriod(program.getProgramRunningPeriod())
                .build();
        log.info("program saved successfully");
        programRepository.save(newProgram);
        log.info("Program '{}' saved successfully under Cohort ID: {}",
                newProgram.getProgramName(), newProgram.getCohort().getId());

    }
}
