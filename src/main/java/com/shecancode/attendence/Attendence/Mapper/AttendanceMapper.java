package com.shecancode.attendence.Attendence.Mapper;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.dto.AttendanceRequest;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;

import java.util.UUID;

public class AttendanceMapper {

    public static Attendance toAttendance(AttendanceRequest attendanceRequest) {
        Attendance attendance = new Attendance();
        attendance.setAttendanceId(UUID.randomUUID());
        attendance.setAttendanceStatus(AttendanceStatus.valueOf(attendanceRequest.getAttendanceStatus()));
        attendance.setAttendanceRecordedDate(attendanceRequest.getAttendanceDate());
        attendance.setCheckInTime(attendanceRequest.getCheckInTime());
        attendance.setRemarks(attendanceRequest.getRemarks());

        return attendance;


    }

    public static AttendanceResponse toResponseDTO(Attendance attendance) {

        if (attendance == null)
            return null;

        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getFullName()) //todo: attend lastname
                .cohortId(attendance.getCohort().getId())
                .cohortNumber(attendance.getCohort().getCohortNumber())

                .programId(attendance.getProgram().getId())
                .programName(attendance.getProgram().getProgramName())

                .attendanceStatus(attendance.getAttendanceStatus().name())
                .checkInTime(attendance.getCheckInTime())
                .remarks(attendance.getRemarks())
                .recordedById(attendance.getRecordedById())
                .recordedByName(attendance.getRecordedByName())

                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())


                .build();
    }
}

