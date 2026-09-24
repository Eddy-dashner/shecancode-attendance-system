package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/students/me/attendance")
@Tag(name = "Students")
public class MyAttendanceController {
    private final AttendanceService attendanceService;

    public MyAttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    @Operation(summary = "Get my attendance (STUDENT only)",
            description = "The authenticated student's attendance history, newest first, with their current score.")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentAttendanceHistoryResponse> getMyAttendance(Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getMyHistory(authentication.getName()));
    }
}
