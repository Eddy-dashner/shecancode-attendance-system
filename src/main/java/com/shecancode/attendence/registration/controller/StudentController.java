package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.StudentRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping( "/api/v1/students")
public class StudentController {
    private final StudentRegistrationService service;


    public StudentController(StudentRegistrationService service) {
        this.service = service;
    }

    @PostMapping("/{cohortNumber}")
    @Operation(summary = "Create Student")
    public ResponseEntity<StudentResponseDao> addStudent(@PathVariable String cohortNumber, @Valid @RequestBody StudentRequestDao studentRequestDao){
        StudentResponseDao registeredStudent = service.createStudent(studentRequestDao, cohortNumber);
        return new ResponseEntity<>(registeredStudent, HttpStatus.CREATED);
    }
}
