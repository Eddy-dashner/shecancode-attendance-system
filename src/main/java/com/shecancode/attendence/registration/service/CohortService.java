package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;

@Slf4j
@Service
@Transactional
public class CohortService {

//    private Cohort cohort;

    private final CohortRepository cohortRepository;

    public CohortService(CohortRepository cohortRepository) {
        this.cohortRepository = cohortRepository;
    }

    public Cohort createCohort(Cohort cohort) {
        log.info("Starting cohort creation for: {}", cohort.getCohortNumber());

        if (cohort.getCohortNumber() == null || cohort.getCohortNumber().isBlank()){
            throw new IllegalArgumentException("Cohort number must not be null ");
        }

        if (cohortRepository.findByCohortNumber(cohort.getCohortNumber()).isPresent()) {
            throw new CohortAlreadyExistException("Cohort with this number exists: " + cohort.getCohortNumber());
        }


        if (cohort.getStartDate() != null && cohort.getEndDate() != null && cohort.getEndDate().isBefore(cohort.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Build the cohort without programs and graduation date (to be added by admin later)
        return cohortRepository.save(Cohort.builder()
                .cohortNumber(cohort.getCohortNumber())
                .startDate(cohort.getStartDate())
                .endDate(cohort.getEndDate())
                        .programs(new ArrayList<>())
                .build());
    }
}


