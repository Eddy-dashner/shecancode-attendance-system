package com.shecancode.attendence.registration.controller;
import com.shecancode.attendence.registration.service.StudentLifeCycleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/students")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Actions performed by an ADMIN: user registration and student administration (invite, bulk-invite, list, drop-out)")
public class StudentLifeCycleController {
    private final StudentLifeCycleService lifeCycleService;

    @PatchMapping("/{studentId}/dropout")
    @Operation(summary = "Mark a student as DROPPED_OUT (ADMIN only)",
            description = "Idempotency is NOT allowed: calling this on an already dropped-out student returns 409.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student marked as DROPPED_OUT"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Student is already DROPPED_OUT", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> markStudentDropout(@PathVariable UUID studentId) {

        lifeCycleService.markDropout(studentId);

        return ResponseEntity.ok("Student marked as DROPPED_OUT");
    }
}
