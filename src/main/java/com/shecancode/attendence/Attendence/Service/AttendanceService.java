package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.AttendanceRepository;
import com.shecancode.attendence.Attendence.dto.AttendanceRequest;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
    private AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    private AttendanceResponse recordAttendance(AttendanceRequest attendanceRequest){

        return null;

    }
}
