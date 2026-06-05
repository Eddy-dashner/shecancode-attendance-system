package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.StudentRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDao> addStudent(@PathVariable String cohortNumber, @Valid @RequestBody StudentRequestDao studentRequestDao){
        StudentResponseDao registeredStudent = service.createStudent(studentRequestDao, cohortNumber);
        log.info("Student created successfully:");
        return new ResponseEntity<>(registeredStudent, HttpStatus.CREATED);

    }

    @GetMapping()
    @Operation(summary = "get all students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StudentResponseDao>> getAllStudents(){
        return ResponseEntity.ok(service.getAllStudents());
    }
}
