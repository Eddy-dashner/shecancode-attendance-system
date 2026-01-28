package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Optional<Object> existByEmail(String email);
}
