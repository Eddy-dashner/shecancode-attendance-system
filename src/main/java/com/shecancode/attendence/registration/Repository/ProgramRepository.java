package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {

    Optional<Program> findByProgramName(String programName);
}
