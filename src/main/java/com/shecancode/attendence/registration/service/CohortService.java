package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Mapper.CohortMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class CohortService {

    private final CohortRepository cohortRepository;
    private final ProgramRepository programRepository;

    public CohortService(CohortRepository cohortRepository, ProgramRepository programRepository) {
        this.cohortRepository = cohortRepository;
        this.programRepository = programRepository;
    }

    public CohortResponseDao createCohort(CohortRequestDao cohortRequestDao) {
        log.info("Starting cohort creation for: {}", LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()));

        if (cohortRequestDao.getCohortNumber() == null || cohortRequestDao.getCohortNumber().isBlank()){
            throw new IllegalArgumentException("Cohort number must not be null ");
        }
        if (cohortRequestDao.getProgramId() == null) {
            throw new IllegalArgumentException("programId is required");
        }

        // A cohort must belong to an existing program (Program 1 ── * Cohort).
        Program program = programRepository.findById(cohortRequestDao.getProgramId())
                .orElseThrow(() -> new ProgramNotFoundException(
                        "Program [" + LoggingUtils.sanitizeForLogging(String.valueOf(cohortRequestDao.getProgramId())) + "] not found."));

        if (cohortRepository.findByCohortNumber(cohortRequestDao.getCohortNumber()).isPresent()) {
            throw new CohortAlreadyExistException("Cohort with this number exists: " + LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()));
        }

        if (cohortRequestDao.getStartDate() != null && cohortRequestDao.getEndDate() != null && cohortRequestDao.getEndDate().isBefore(cohortRequestDao.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        Cohort cohort = Cohort.builder()
                .id(UUID.randomUUID())
                .cohortNumber(cohortRequestDao.getCohortNumber())
                .startDate(cohortRequestDao.getStartDate())
                .endDate(cohortRequestDao.getEndDate())
                .program(program)
                .build();

        Cohort savedCohort = cohortRepository.save(cohort);
        log.info("Cohort created successfully: {}", LoggingUtils.sanitizeForLogging(savedCohort.getCohortNumber()));
        savedCohort.setCohortNumber(savedCohort.getCohortNumber());
        return CohortMapper.toCohortResponseDao(savedCohort);
    }

    public List<CohortResponseDao> getAllCohorts() {
        return cohortRepository.findAll().stream()
                .map(CohortMapper::toCohortResponseDao)
                .collect(Collectors.toList());
    }
}


