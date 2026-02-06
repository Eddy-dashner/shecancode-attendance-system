package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.service.ProgramService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping
@RestController
public class ProgramController {
    private ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping
    public ResponseEntity<String> CreateProgram(@RequestBody Program program){
       programService.createProgram(program);
       log.info("program created");
       return new ResponseEntity<>("Program created", HttpStatus.ACCEPTED);

    }
}
