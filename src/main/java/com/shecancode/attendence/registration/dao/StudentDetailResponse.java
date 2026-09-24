package com.shecancode.attendence.registration.dao;

import com.shecancode.attendence.Attendence.dao.AttendanceSummaryDto;

/** One student's profile plus their attendance summary in their current program. */
public record StudentDetailResponse(StudentResponseDao student, AttendanceSummaryDto attendance) {}
