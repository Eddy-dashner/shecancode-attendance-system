package com.shecancode.attendence.registration.controller;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.dao.StudentStatusRequest;
import jakarta.validation.Valid;
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
@Tag(name = "Students")
public class StudentLifeCycleController {
    private final StudentLifeCycleService lifeCycleService;

    @PatchMapping("/{studentId}/dropout")
    @Operation(summary = "Mark a student as dropped out (ADMIN only)",
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

    @PatchMapping("/{studentId}/status")
    @Operation(summary = "Change a student's status (ADMIN only)",
            description = "ACTIVE -> INACTIVE, DROPPED_OUT or GRADUATED; INACTIVE -> ACTIVE or DROPPED_OUT; " +
                    "PENDING -> DROPPED_OUT. GRADUATED and DROPPED_OUT are final. PENDING becomes ACTIVE only when " +
                    "the student activates. INACTIVE and GRADUATED students cannot be marked for attendance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status changed"),
            @ApiResponse(responseCode = "400", description = "Missing or unknown status", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "Change not allowed from the current status, or the cohort is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDao> changeStatus(@PathVariable UUID studentId,
                                                           @Valid @RequestBody StudentStatusRequest request) {
        return ResponseEntity.ok(lifeCycleService.changeStatus(studentId, request.getStatus()));
    }

    @PutMapping("/{studentId}")
    @Operation(summary = "Update a student (ADMIN only)",
            description = "Replaces the profile fields. Email, program and cohort are not changed here. " +
                    "Not allowed while the student's cohort is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "The student's cohort is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDao> updateStudent(@PathVariable UUID studentId,
                                                            @Valid @RequestBody CompleteProfileRequest request) {
        return ResponseEntity.ok(lifeCycleService.updateStudent(studentId, request));
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "Delete a student (ADMIN only)",
            description = "Soft delete: the student is hidden everywhere and can no longer log in, but their " +
                    "attendance history is kept. Not allowed while their cohort is closed.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Student deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "The student's cohort is closed", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable UUID studentId) {
        lifeCycleService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }
}
