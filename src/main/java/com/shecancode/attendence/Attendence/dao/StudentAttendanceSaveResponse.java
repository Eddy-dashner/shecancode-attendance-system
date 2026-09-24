package com.shecancode.attendence.Attendence.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Result of saving one student's attendance: the record and any alerts it raised. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentAttendanceSaveResponse {
    private AttendanceResponse record;
    private List<AttendanceAlertResponse> alertsRaised;
}
