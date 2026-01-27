package com.shecancode.attendence.AttendenceMgt.Repository;

import com.shecancode.attendence.AttendenceMgt.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
}
