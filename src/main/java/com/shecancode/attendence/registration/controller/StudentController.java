package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.AdminCreateStudentRequest;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.StudentRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @PostMapping
    @Operation(tags = {"Admin"}, summary = "Invite a student (ADMIN only)",
            description = "Creates a PENDING student and a disabled login account from just an email, program and " +
                    "cohort, then emails the student an activation link. The program and cohort must already exist, " +
                    "and the cohort must belong to the program.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student invited; activation email sent"),
            @ApiResponse(responseCode = "400", description = "Validation failed, or cohort does not belong to program", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not an ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Cohort or program not found", content = @Content),
            @ApiResponse(responseCode = "409", description = "A student with this email already exists", content = @Content),
            @ApiResponse(responseCode = "502", description = "Invitation email could not be sent", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentResponseDao> addStudent(@Valid @RequestBody AdminCreateStudentRequest request){
        StudentResponseDao registeredStudent = service.createStudentAccount(request);
        log.info("Student invited successfully");
        return new ResponseEntity<>(registeredStudent, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(tags = {"Student"}, summary = "Get my profile (STUDENT only)",
            description = "Returns the authenticated student's own profile.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "No student record for this account", content = @Content)
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponseDao> getMyProfile(Authentication authentication){
        return ResponseEntity.ok(service.getMyProfile(authentication.getName()));
    }

    @PutMapping("/me/profile")
    @Operation(tags = {"Student"}, summary = "Complete my profile (STUDENT only)",
            description = "The authenticated student fills in their remaining details after activating. " +
                    "Moves the record from PENDING to ACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile completed"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is not a STUDENT", content = @Content),
            @ApiResponse(responseCode = "404", description = "No student record for this account", content = @Content)
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentResponseDao> completeMyProfile(Authentication authentication,
                                                               @Valid @RequestBody CompleteProfileRequest request){
        return ResponseEntity.ok(service.completeProfile(authentication.getName(), request));
    }

    @GetMapping()
    @Operation(tags = {"Admin"}, summary = "List all students (ADMIN or TRAINER)",
            description = "Returns all registered students. TRAINER has read-only access per the role model.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT (not permitted)", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<List<StudentResponseDao>> getAllStudents(){
        return ResponseEntity.ok(service.getAllStudents());
    }
}
