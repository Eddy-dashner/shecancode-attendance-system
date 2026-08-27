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

@Slf4j
@RestController
@RequestMapping("/api/v1/cohorts")
@Tag(name = "Programs", description = "Program management within a cohort (ADMIN only)")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{cohortNumber}/program")
    @Operation(summary = "Create a program under a cohort (ADMIN only)",
            description = "Adds a program to the cohort identified by cohortNumber. Program name must be unique; " +
                    "programEndDate must not be before programStartDate.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Program created"),
            @ApiResponse(responseCode = "400", description = "Blank program name, duplicate name, or end date before start date", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgramResponseDao> CreateProgram(@PathVariable String cohortNumber,
                                                            @Valid @RequestBody ProgramRequestDao  requestDao){

        log.info("program created : {} ", LoggingUtils.sanitizeForLogging(cohortNumber));
       ProgramResponseDao saveResponse =programService.createProgram( cohortNumber, requestDao);
       return new ResponseEntity<>(saveResponse, HttpStatus.CREATED);

    }
}
