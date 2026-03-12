package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
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
    protected ResponseEntity<CohortResponseDao> createCohort(@RequestBody CohortRequestDao cohortRequestDao){
        log.info("Received cohort request: {}", cohortRequestDao.getCohortNumber());

      CohortResponseDao saved =  cohortService.createCohort(cohortRequestDao);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

}
