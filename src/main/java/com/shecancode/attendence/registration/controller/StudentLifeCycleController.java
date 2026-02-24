package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.service.StudentLifeCycleService;
import com.shecancode.attendence.registration.service.StudentRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/students")
@RequiredArgsConstructor
public class StudentLifeCycleController {
    private final StudentLifeCycleService lifeCycleService;

    @PatchMapping("/{studentId}/dropout")
    public ResponseEntity<String> markStudentDropout(@PathVariable UUID studentId) {

        lifeCycleService.markDropout(studentId);

        return ResponseEntity.ok("Student marked as DROPPED_OUT");
    }
}
