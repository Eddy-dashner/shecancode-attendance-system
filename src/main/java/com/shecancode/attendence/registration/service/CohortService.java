package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Slf4j
@Service
@Transactional
public class CohortService {

//    private Cohort cohort;

    private final CohortRepository cohortRepository;
    private final ProgramRepository programRepository;


    public CohortService(CohortRepository cohortRepository, ProgramRepository programRepository) {
        this.cohortRepository = cohortRepository;
        this.programRepository = programRepository;
    }

    public Cohort createCohort(Cohort cohort) {
        log.info("Starting cohort creation for: {}", cohort.getCohortNumber());

        if (cohortRepository.findByCohortNumber(cohort.getCohortNumber()).isPresent()) {
            throw new CohortAlreadyExistException("Cohort with this number exists: " + cohort.getCohortNumber());
        }

        // Build the cohort without programs (as per your requirement)
        Cohort savecohort = cohortRepository.save(Cohort.builder()
                .cohortNumber(cohort.getCohortNumber())
                .startDate(cohort.getStartDate())
                .endDate(cohort.getEndDate())
                .graduationDate(cohort.getGraduationDate())
                .programs(new ArrayList<>()) // Explicitly empty
                .build());

        log.info("Cohort created successfully with ID: {}", savecohort.getId());
        return savecohort;

    }
}


