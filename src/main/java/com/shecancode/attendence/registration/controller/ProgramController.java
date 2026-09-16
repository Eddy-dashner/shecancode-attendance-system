package com.shecancode.attendence.registration.controller;


import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
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
    public ResponseEntity<List<ProgramResponseDao>> getAllPrograms() {
        log.info("Retrieving all programs");
        return ResponseEntity.ok(programService.getAllPrograms());
    }

    @GetMapping("/{programId}")
    @Operation(summary = "Get a program by id (ADMIN only)")
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
}
