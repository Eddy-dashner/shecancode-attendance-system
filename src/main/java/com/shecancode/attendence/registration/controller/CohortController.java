package com.shecancode.attendence.registration.controller;

<<<<<<< HEAD
=======
import com.shecancode.attendence.registration.Model.Cohort;
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.service.CohortService;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.security.access.prepost.PreAuthorize;
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
<<<<<<< HEAD
@RequestMapping("/api/v1/cohorts")
=======
@RequestMapping("/api/v1/cohort")
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
public class CohortController {
    private final  CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }

<<<<<<< HEAD
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCohorts() {
        log.info("Retrieving all cohorts");
        return ResponseEntity.ok(cohortService.getAllCohorts());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
=======
    @PostMapping
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
    protected ResponseEntity<CohortResponseDao> createCohort(@RequestBody CohortRequestDao cohortRequestDao){
        log.info("Received cohort request: {}", LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()));

      CohortResponseDao saved =  cohortService.createCohort(cohortRequestDao);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

}
