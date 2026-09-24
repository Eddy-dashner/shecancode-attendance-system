package com.shecancode.attendence.registration.Repository;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Model.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {

    Optional<Program> findFirstByProgramName(String programName);

    // Deleted programs still reserve their name.
    boolean existsByProgramName(String programName);

    boolean existsByProgramNameAndIdNot(String programName, UUID id);

    Optional<Program> findByIdAndDeletedAtIsNull(UUID id);

    List<Program> findByDeletedAtIsNullOrderByProgramStartDateDesc();

    List<Program> findByStatusAndDeletedAtIsNullOrderByProgramStartDateDesc(LifecycleStatus status);
}
