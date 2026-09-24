package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.dao.CohortProgressResponse;
import com.shecancode.attendence.registration.dao.LifecycleStatusRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
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

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/cohort")
@Tag(name = "Administration")
public class CohortController {
    private final  CohortService cohortService;

    public CohortController(CohortService cohortService) {
        this.cohortService = cohortService;
    }
    @GetMapping
    @Operation(summary = "List cohorts (ADMIN only)", description = "Optionally filter by programId and status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CohortResponseDao>> getAllCohorts(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) LifecycleStatus status) {
        log.info("Retrieving all cohorts");
        return ResponseEntity.ok(cohortService.getAllCohorts(programId, status));
    }

    @GetMapping("/{cohortId}")
    @Operation(summary = "Get a cohort by id (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cohort returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CohortResponseDao> getCohortById(@PathVariable UUID cohortId) {
        log.info("Retrieving cohort [{}]", cohortId);
        return ResponseEntity.ok(cohortService.getCohortById(cohortId));
    }

    @PostMapping
    @Operation(summary = "Create a cohort (ADMIN only)",
            description = "Creates a new cohort. Cohort number must be unique; endDate must not be before startDate.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cohort created successfully"),
            @ApiResponse(responseCode = "400", description = "Blank cohort number or endDate before startDate", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "The referenced program was not found", content = @Content),
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

    @PutMapping("/{cohortId}")
    @Operation(summary = "Update a cohort (ADMIN only)",
            description = "Replaces number and dates. programId must be the cohort's current program. " +
                    "Not allowed while the cohort or its program is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cohort updated"),
            @ApiResponse(responseCode = "400", description = "Blank number, end date before start date, or a different programId", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cohort is closed, or the number is already used in the program", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CohortResponseDao> updateCohort(@PathVariable UUID cohortId,
                                                          @Valid @RequestBody CohortRequestDao request) {
        return ResponseEntity.ok(cohortService.updateCohort(cohortId, request));
    }

    @PatchMapping("/{cohortId}/status")
    @Operation(summary = "Open or close a cohort (ADMIN only)",
            description = "CLOSED makes the cohort read-only (view only: no updates, students or attendance). " +
                    "OPEN reopens it, which is refused while its program is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Missing or unknown status", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Reopening while the program is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CohortResponseDao> changeStatus(@PathVariable UUID cohortId,
                                                          @Valid @RequestBody LifecycleStatusRequest request) {
        return ResponseEntity.ok(cohortService.changeStatus(cohortId, request.getStatus()));
    }

    @DeleteMapping("/{cohortId}")
    @Operation(summary = "Delete a cohort (ADMIN only)",
            description = "Soft delete: the cohort is hidden but kept in the database. Refused while it still has " +
                    "students or is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cohort deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Cohort still has students, or is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCohort(@PathVariable UUID cohortId) {
        cohortService.deleteCohort(cohortId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cohortId}/progress")
    @Operation(summary = "Cohort progress (ADMIN or TRAINER)",
            description = "Which week the cohort is in, computed from its dates. currentWeek is 0 before it starts.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<CohortProgressResponse> getProgress(@PathVariable UUID cohortId) {
        return ResponseEntity.ok(cohortService.getProgress(cohortId));
    }

    @GetMapping("/{cohortId}/participants")
    @Operation(summary = "List a cohort's participants (ADMIN or TRAINER)",
            description = "Students of the cohort, ordered by name. Defaults to ACTIVE students (participants).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<StudentResponseDao>> getParticipants(
            @PathVariable UUID cohortId,
            @RequestParam(defaultValue = "ACTIVE") Status status) {
        return ResponseEntity.ok(cohortService.getParticipants(cohortId, status));
    }

}
