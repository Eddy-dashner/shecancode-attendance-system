package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
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

    /**
     * @param cohortNumber
     * @param program
     * @return
     */
    public Program createProgram(String cohortNumber, Program program){

        Cohort cohort = cohortRepository.findByCohortNumber(cohortNumber).orElseThrow(()
                -> new CohortNotFoundException(" cohort not found exception"));

        if (program.getId() != null && programRepository.existsById(program.getId())) {
            throw new IllegalArgumentException("Program already exists with ID: " + program.getId());
        }
        if (program.getProgramStartDate() != null && program.getProgramEndDate().isBefore(program.getProgramStartDate())){
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        Program newProgram = Program.builder()
                .id(UUID.randomUUID())
                .programName(program.getProgramName())
                .programDuration(program.getProgramDuration())
                .cohort(cohort)
                .programStartDate(program.getProgramStartDate())
                .programEndDate(program.getProgramEndDate())
//                .daysRemainingUntilGraduation(program.getDaysRemainingUntilGraduation())
                .build();
        log.info("program saved successfully");
        log.info("saving a program under the cohort: {} ", newProgram.getProgramName(),cohortNumber);

        return programRepository.save(newProgram);

    }

}
