package com.shecancode.attendence.registration.controller;

<<<<<<< HEAD
=======
import com.shecancode.attendence.registration.Model.Program;
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import com.shecancode.attendence.registration.service.ProgramService;
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
@RequestMapping("/api/v1/cohorts")
public class ProgramController {
<<<<<<< HEAD
    private final ProgramService programService;
=======
    private ProgramService programService;
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping("/{cohortNumber}/program")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
    public ResponseEntity<ProgramResponseDao> CreateProgram(@PathVariable String cohortNumber,
                                                            @RequestBody ProgramRequestDao  requestDao){

        log.info("program created : {} ", LoggingUtils.sanitizeForLogging(cohortNumber));
       ProgramResponseDao saveResponse =programService.createProgram( cohortNumber, requestDao);
       return new ResponseEntity<>(saveResponse, HttpStatus.CREATED);

    }
<<<<<<< HEAD


=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
}
