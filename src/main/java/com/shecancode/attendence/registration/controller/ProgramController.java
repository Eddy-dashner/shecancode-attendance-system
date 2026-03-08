package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.service.ProgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/cohorts")
public class ProgramController {
    private ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{cohortNumber}/program")
    public ResponseEntity<String> CreateProgram(@PathVariable String cohortNumber,
                                                @RequestBody Program program){

        log.info("program created : {} ",  cohortNumber);
       programService.createProgram( cohortNumber, program);

       return new ResponseEntity<>("Program created", HttpStatus.ACCEPTED);

    }
}
