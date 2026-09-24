package com.shecancode.attendence.registration.controller;

import com.shecancode.attendence.registration.dao.AdminCreateStudentRequest;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.dao.PageResponse;
import com.shecancode.attendence.registration.dao.StudentDetailResponse;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.service.StudentLifeCycleService;
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

import java.util.UUID;

@RestController
@RequestMapping( "/api/v1/students")
@Slf4j
public class StudentController {
    private final StudentRegistrationService service;
    private final StudentLifeCycleService lifeCycleService;

    public StudentController(StudentRegistrationService service, StudentLifeCycleService lifeCycleService) {
        this.service = service;
        this.lifeCycleService = lifeCycleService;
    }

    @PostMapping
    @Operation(tags = {"Students"}, summary = "Invite a student (ADMIN only)",
            description = "Creates a PENDING student and a disabled login account from an email, program " +
                    "and cohort, then emails the student an activation link. The program and cohort must already " +
                    "exist, and the cohort must belong to the program.")
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
    @Operation(tags = {"Students"}, summary = "Get my profile (STUDENT only)",
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
    @Operation(tags = {"Students"}, summary = "Complete my profile (STUDENT only)",
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
    @Operation(tags = {"Students"}, summary = "Search students (ADMIN or TRAINER)",
            description = "Paged list ordered by name. All filters are optional; search matches email, first or " +
                    "last name. page starts at 0; size is 1-100.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page returned"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size", content = @Content),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT (not permitted)", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<PageResponse<StudentResponseDao>> getAllStudents(
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID cohortId,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(lifeCycleService.search(programId, cohortId, status, search, page, size));
    }

    @GetMapping("/{studentId}")
    @Operation(tags = {"Students"}, summary = "Get one student (ADMIN or TRAINER)",
            description = "The student's profile with their attendance summary in their program.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
            @ApiResponse(responseCode = "403", description = "Caller is a STUDENT (not permitted)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<StudentDetailResponse> getStudent(@PathVariable UUID studentId) {
        return ResponseEntity.ok(lifeCycleService.getStudent(studentId));
    }
}
