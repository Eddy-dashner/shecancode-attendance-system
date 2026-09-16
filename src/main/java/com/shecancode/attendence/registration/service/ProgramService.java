package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Mapper.ProgramMapper;
import com.shecancode.attendence.registration.Model.Program;
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


    public ProgramService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    // A program stands on its own and does not require a cohort; cohorts are
    // attached to a program later via cohort creation.
    public ProgramResponseDao createProgram(ProgramRequestDao programRequest){

        if (programRepository.existsByProgramName(programRequest.getProgramName())) {
            log.error("Duplicate program name: {}", LoggingUtils.sanitizeForLogging(programRequest.getProgramName()));
            throw new IllegalArgumentException("A program named '" + programRequest.getProgramName() + "' already exists.");
        }

        if (programRequest.getProgramStartDate() != null && programRequest.getProgramEndDate() != null) {
            if (programRequest.getProgramEndDate().isBefore(programRequest.getProgramStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
        }
        Program newProgram = Program.builder()
                .id(UUID.randomUUID())
                .programName(programRequest.getProgramName())
                .programDuration(programRequest.getProgramDuration())
                .programStartDate(programRequest.getProgramStartDate())
                .programEndDate(programRequest.getProgramEndDate())
                .build();
        Program saveProgram = programRepository.save(newProgram);
        log.info("program {} saved successfully", LoggingUtils.sanitizeForLogging(newProgram.getProgramName()));

        return ProgramMapper.ToResponseDao(saveProgram);

    }


    public List<ProgramResponseDao> getAllPrograms() {
        return programRepository.findAll().stream()
                .map(ProgramMapper::ToResponseDao)
                .toList();
    }

    public ProgramResponseDao getProgramById(UUID programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ProgramNotFoundException("Program [" + programId + "] not found."));
        return ProgramMapper.ToResponseDao(program);
    }
}
