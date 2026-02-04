package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.service.CohortService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cohort")
public class CohortController {
    private final  CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }

    @PostMapping
    protected ResponseEntity<Cohort> createCohort(@RequestBody Cohort cohort){
        log.info("Received cohort request: {}", cohort.getCohortNumber());

       Cohort savedCohort=  cohortService.createCohort(cohort);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedCohort);
    }
}
