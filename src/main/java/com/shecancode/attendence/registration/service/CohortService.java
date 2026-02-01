package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Programs;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CohortService {

//    private Cohort cohort;

    private final CohortRepository cohortRepository;

    public CohortService(CohortRepository cohortRepository) {
        this.cohortRepository = cohortRepository;
    }


    public Cohort createCohort(Cohort cohort){

        Cohort savecohort = Cohort.builder()
                .id(UUID.randomUUID())
                .cohortNumber(cohort.getCohortNumber())
                .startDate(cohort.getStartDate())
                .endDate(cohort.getEndDate())
                .graduationDate(cohort.getGraduationDate())
                .program(cohort.getProgram())
                .build();
        
        return cohortRepository.save(savecohort);
    }
}
