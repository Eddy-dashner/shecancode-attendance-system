package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Mapper.ProgramMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProgramService {
    private final ProgramRepository programRepository;

    private final CohortRepository cohortRepository;


    public ProgramService(ProgramRepository programRepository, CohortRepository cohortRepository) {
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
    }

    /**
     * @param cohortNumber
     * @param programRequest
     * @return
     */
    public ProgramResponseDao createProgram(String cohortNumber, ProgramRequestDao programRequest){

        Cohort cohort = cohortRepository.findByCohortNumber(cohortNumber).orElseThrow(()
                -> new CohortNotFoundException(" cohort not found exception"));

        // 1. Check if ID exists (Note: Usually for 'Create', we don't pass an ID)
        if (programRepository.existsByProgramName(programRequest.getProgramName())) {
            log.error("Duplicate program name: {}", LoggingUtils.sanitizeForLogging(programRequest.getProgramName()));
            throw new IllegalArgumentException("A program named '" + programRequest.getProgramName() + "' already exists.");
        }

// 2. Validate Dates using the request object
        if (programRequest.getProgramStartDate() != null && programRequest.getProgramEndDate() != null) {
            if (programRequest.getProgramEndDate().isBefore(programRequest.getProgramStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
        Program newProgram = Program.builder()
                .id(UUID.randomUUID())
                .programName(programRequest.getProgramName())
                .programDuration(programRequest.getProgramDuration())
                .cohort(cohort)
                .programStartDate(programRequest.getProgramStartDate())
                .programEndDate(programRequest.getProgramEndDate())
//                .daysRemainingUntilGraduation(program.getDaysRemainingUntilGraduation())
                .build();
        Program saveProgram = programRepository.save(newProgram);
        log.info("program saved successfully");
        log.info("saving a program {} under the cohort: {} ", LoggingUtils.sanitizeForLogging(newProgram.getProgramName()), LoggingUtils.sanitizeForLogging(cohortNumber));

        return ProgramMapper.ToResponseDao(saveProgram);

    }


    public List<ProgramResponseDao> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(ProgramMapper::ToResponseDao)
                .toList();
    }
}
