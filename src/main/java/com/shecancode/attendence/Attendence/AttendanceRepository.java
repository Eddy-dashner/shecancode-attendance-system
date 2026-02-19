package com.shecancode.attendence.Attendence;

import com.shecancode.attendence.Attendence.Model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
}
