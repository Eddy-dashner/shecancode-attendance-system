package com.shecancode.attendence.registration.controller;


import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import com.shecancode.attendence.registration.service.ProgramService;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cohorts")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{cohortNumber}/program")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<ProgramResponseDao> CreateProgram(@PathVariable String cohortNumber,
                                                            @RequestBody ProgramRequestDao  requestDao){

        log.info("program created : {} ", LoggingUtils.sanitizeForLogging(cohortNumber));
       ProgramResponseDao saveResponse =programService.createProgram( cohortNumber, requestDao);
       return new ResponseEntity<>(saveResponse, HttpStatus.CREATED);

    }
}
