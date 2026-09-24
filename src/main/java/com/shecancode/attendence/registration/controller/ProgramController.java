package com.shecancode.attendence.registration.controller;


import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.dao.LifecycleStatusRequest;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.ProgramService;
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
@RequestMapping("/api/v1/programs")
@Tag(name = "Administration")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    @Operation(summary = "List all programs (ADMIN only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ProgramResponseDao>> getAllPrograms(
            @RequestParam(required = false) LifecycleStatus status) {
        log.info("Retrieving all programs");
        return ResponseEntity.ok(programService.getAllPrograms(status));
    }

    @GetMapping("/{programId}")
    @Operation(summary = "Get a program by id (ADMIN only)",
            description = "Includes status, number of cohorts and number of participants (ACTIVE students).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponseDao> getProgramById(@PathVariable UUID programId) {
        log.info("Retrieving program [{}]", programId);
        return ResponseEntity.ok(programService.getProgramById(programId));
    }

    @PostMapping
    @Operation(summary = "Create a program (ADMIN only)",
            description = "Creates a program. A program does not require a cohort. Program name must be unique; " +
                    "programEndDate must not be before programStartDate.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Program created successfully"),
            @ApiResponse(responseCode = "400", description = "Blank program name, duplicate name, or end date before start date", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponseDao> CreateProgram(@Valid @RequestBody ProgramRequestDao requestDao){

        log.info("Creating program: {}", LoggingUtils.sanitizeForLogging(requestDao.getProgramName()));
       ProgramResponseDao saveResponse = programService.createProgram(requestDao);
       return new ResponseEntity<>(saveResponse, HttpStatus.CREATED);

    }

    @PutMapping("/{programId}")
    @Operation(summary = "Update a program (ADMIN only)",
            description = "Replaces name, duration and dates. Not allowed while the program is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Program updated"),
            @ApiResponse(responseCode = "400", description = "Blank or duplicate name, or end date before start date", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Program is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponseDao> updateProgram(@PathVariable UUID programId,
                                                            @Valid @RequestBody ProgramRequestDao request) {
        return ResponseEntity.ok(programService.updateProgram(programId, request));
    }

    @PatchMapping("/{programId}/status")
    @Operation(summary = "Open or close a program (ADMIN only)",
            description = "CLOSED makes the program and all of its cohorts read-only (view only: no updates, new " +
                    "cohorts, students or attendance). OPEN reopens it.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Missing or unknown status", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponseDao> changeStatus(@PathVariable UUID programId,
                                                           @Valid @RequestBody LifecycleStatusRequest request) {
        return ResponseEntity.ok(programService.changeStatus(programId, request.getStatus()));
    }

    @DeleteMapping("/{programId}")
    @Operation(summary = "Delete a program (ADMIN only)",
            description = "Soft delete: the program is hidden but kept in the database. Refused while it still has cohorts.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Program deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Program still has cohorts", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProgram(@PathVariable UUID programId) {
        programService.deleteProgram(programId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{programId}/participants")
    @Operation(summary = "List a program's participants (ADMIN or TRAINER)",
            description = "Students of the program, ordered by name. Defaults to ACTIVE students (participants); " +
                    "filter by cohortId or another status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "Program not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<StudentResponseDao>> getParticipants(
            @PathVariable UUID programId,
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(defaultValue = "ACTIVE") Status status) {
        return ResponseEntity.ok(programService.getParticipants(programId, cohortId, status));
    }
}
