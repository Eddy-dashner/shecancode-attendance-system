package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.StudentRegistrationService;
<<<<<<< HEAD
=======
import io.micrometer.common.util.internal.logging.Slf4JLoggerFactory;
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
=======
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4

@RestController
@RequestMapping( "/api/v1/students")
@Slf4j
public class StudentController {
    private final StudentRegistrationService service;


    public StudentController(StudentRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/{cohortNumber}")
    @Operation(summary = "Create Student")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
    public ResponseEntity<StudentResponseDao> addStudent(@PathVariable String cohortNumber, @Valid @RequestBody StudentRequestDao studentRequestDao){
        StudentResponseDao registeredStudent = service.createStudent(studentRequestDao, cohortNumber);
        log.info("Student created successfully:");
        return new ResponseEntity<>(registeredStudent, HttpStatus.CREATED);

    }
<<<<<<< HEAD

    @GetMapping()
    @Operation(summary = "get all students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponseDao>> getAllStudents(){
        return ResponseEntity.ok(service.getAllStudents());
    }
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
}
