package com.shecancode.attendence.Attendence.Controller;

import com.shecancode.attendence.Attendence.Service.AttendanceService;
import com.shecancode.attendence.Attendence.dto.AttendanceRequest;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping({"/record"})
    public ResponseEntity<AttendanceResponse> recordAttendance(@RequestBody AttendanceRequest attendanceRequest){
        attendanceService.recordAttendance(attendanceRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);

    }
}
