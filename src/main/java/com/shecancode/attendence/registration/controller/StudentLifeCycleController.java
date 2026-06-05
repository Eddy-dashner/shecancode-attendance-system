package com.shecancode.attendence.registration.controller;
import com.shecancode.attendence.registration.service.StudentLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
<<<<<<< HEAD
import org.springframework.security.access.prepost.PreAuthorize;
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/students")
@RequiredArgsConstructor
public class StudentLifeCycleController {
    private final StudentLifeCycleService lifeCycleService;

    @PatchMapping("/{studentId}/dropout")
<<<<<<< HEAD
    @PreAuthorize("hasRole('ADMIN')")
=======
>>>>>>> 9327538160dac42747dd38ffc4bfe9034b75a9e4
    public ResponseEntity<String> markStudentDropout(@PathVariable UUID studentId) {

        lifeCycleService.markDropout(studentId);

        return ResponseEntity.ok("Student marked as DROPPED_OUT");
    }
}
