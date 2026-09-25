package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
    Optional<StudentProfile> findByStudent_Id(UUID studentId);
}
