package com.shecancode.attendence.Attendence.Repo;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByStudentAndProgramOrderByAttendanceRecordedDateDesc(Student student, Program program);

    int countByStudentAndProgramAndAttendanceStatus(Student student, Program program, AttendanceStatus attendanceStatus);

}
