package com.shecancode.attendence.registration.controller;
import com.shecancode.attendence.registration.service.StudentLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/students")
@RequiredArgsConstructor
public class StudentLifeCycleController {
    private final StudentLifeCycleService lifeCycleService;

    @PatchMapping("/{studentId}/dropout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> markStudentDropout(@PathVariable UUID studentId) {

        lifeCycleService.markDropout(studentId);

        return ResponseEntity.ok("Student marked as DROPPED_OUT");
    }
}
