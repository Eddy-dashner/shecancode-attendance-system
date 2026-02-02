package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.service.CohortService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cohort")
public class CohortController {
    private final  CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }

    @PostMapping
    protected ResponseEntity<String> createCohort(@RequestBody Cohort cohort){
        cohortService.createCohort(cohort);
        return new ResponseEntity<>("Cohort-created-Successfully", HttpStatus.OK);
    }
}
