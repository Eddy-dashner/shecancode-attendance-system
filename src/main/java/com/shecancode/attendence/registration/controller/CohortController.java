package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.service.CohortService;
import com.shecancode.attendence.registration.util.LoggingUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cohort")
@Tag(name = "Cohorts", description = "Cohort management (ADMIN only)")
public class CohortController {
    private final  CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }
    @GetMapping
    @Operation(summary = "List all cohorts (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCohorts() {
        log.info("Retrieving all cohorts");
        return ResponseEntity.ok(cohortService.getAllCohorts());
    }

    @PostMapping
    @Operation(summary = "Create a cohort (ADMIN only)",
            description = "Creates a new cohort. Cohort number must be unique; endDate must not be before startDate.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cohort created"),
            @ApiResponse(responseCode = "400", description = "Blank cohort number or endDate before startDate", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "409", description = "A cohort with this number already exists", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CohortResponseDao> createCohort(@Valid @RequestBody CohortRequestDao cohortRequestDao){
        log.info("Received cohort request: {}", LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()));

      CohortResponseDao saved =  cohortService.createCohort(cohortRequestDao);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

}
