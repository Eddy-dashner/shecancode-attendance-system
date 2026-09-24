package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Exception.ReadOnlyException;
import com.shecancode.attendence.registration.Exception.ResourceInUseException;
import com.shecancode.attendence.registration.Mapper.ProgramMapper;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.Repository.StudentSpecs;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProgramService {
    private final ProgramRepository programRepository;
    private final CohortRepository cohortRepository;
    private final StudentRepository studentRepository;

    public ProgramService(ProgramRepository programRepository, CohortRepository cohortRepository,
                          StudentRepository studentRepository) {
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
        this.studentRepository = studentRepository;
    }

    public ProgramResponseDao createProgram(ProgramRequestDao programRequest){

        if (programRepository.existsByProgramName(programRequest.getProgramName())) {
            log.error("Duplicate program name: {}", LoggingUtils.sanitizeForLogging(programRequest.getProgramName()));
            throw new IllegalArgumentException("A program named '" + programRequest.getProgramName() + "' already exists.");
        }

        validateDates(programRequest);
        Program newProgram = Program.builder()
                .id(UUID.randomUUID())
                .programName(programRequest.getProgramName())
                .programDuration(programRequest.getProgramDuration())
                .programStartDate(programRequest.getProgramStartDate())
                .programEndDate(programRequest.getProgramEndDate())
                .build();
        Program saveProgram = programRepository.save(newProgram);
        log.info("program {} saved successfully", LoggingUtils.sanitizeForLogging(newProgram.getProgramName()));

        return ProgramMapper.ToResponseDao(saveProgram);

    }

    /** @param status optional filter; null lists every (non-deleted) program */
    public List<ProgramResponseDao> getAllPrograms(LifecycleStatus status) {
        List<Program> programs = status == null
                ? programRepository.findByDeletedAtIsNullOrderByProgramStartDateDesc()
                : programRepository.findByStatusAndDeletedAtIsNullOrderByProgramStartDateDesc(status);
        return programs.stream()
                .map(ProgramMapper::ToResponseDao)
                .toList();
    }

    public ProgramResponseDao getProgramById(UUID programId) {
        Program program = findProgram(programId);
        ProgramResponseDao response = ProgramMapper.ToResponseDao(program);
        response.setCohortCount(cohortRepository.countByProgram_IdAndDeletedAtIsNull(programId));
        response.setParticipantCount(
                studentRepository.countByProgram_IdAndStatusAndDeletedAtIsNull(programId, Status.ACTIVE));
        return response;
    }

    @Transactional
    public ProgramResponseDao updateProgram(UUID programId, ProgramRequestDao request) {
        Program program = findProgram(programId);
        requireOpen(program);
        if (programRepository.existsByProgramNameAndIdNot(request.getProgramName(), programId)) {
            throw new IllegalArgumentException("A program named '" + request.getProgramName() + "' already exists.");
        }
        validateDates(request);

        program.setProgramName(request.getProgramName());
        program.setProgramDuration(request.getProgramDuration());
        program.setProgramStartDate(request.getProgramStartDate());
        program.setProgramEndDate(request.getProgramEndDate());
        log.info("Program [{}] updated", programId);
        return ProgramMapper.ToResponseDao(programRepository.save(program));
    }

    /** Closing makes the program and all of its cohorts read-only; reopening lifts that. */
    @Transactional
    public ProgramResponseDao changeStatus(UUID programId, LifecycleStatus status) {
        Program program = findProgram(programId);
        program.setStatus(status);
        log.info("Program [{}] is now {}", programId, status);
        return ProgramMapper.ToResponseDao(programRepository.save(program));
    }

    /** Soft delete. Refused while the program still has cohorts, so nothing is left orphaned. */
    @Transactional
    public void deleteProgram(UUID programId) {
        Program program = findProgram(programId);
        long cohorts = cohortRepository.countByProgram_IdAndDeletedAtIsNull(programId);
        if (cohorts > 0) {
            throw new ResourceInUseException("Program still has " + cohorts + " cohort(s); delete them first.");
        }
        program.setDeletedAt(Instant.now());
        programRepository.save(program);
        log.info("Program [{}] deleted", programId);
    }

    /**
     * Students of the program, ordered by name.
     *
     * @param status optional; defaults to ACTIVE (participants) in the controller
     */
    public List<StudentResponseDao> getParticipants(UUID programId, UUID cohortId, Status status) {
        findProgram(programId);
        return studentRepository.findAll(StudentSpecs.matching(programId, cohortId, status, null),
                        Sort.by("studentFirstName", "studentLastName")).stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    private Program findProgram(UUID programId) {
        return programRepository.findByIdAndDeletedAtIsNull(programId)
                .orElseThrow(() -> new ProgramNotFoundException("Program [" + programId + "] not found."));
    }

    private static void requireOpen(Program program) {
        if (program.isClosed()) {
            throw new ReadOnlyException("Program '" + program.getProgramName() + "' is closed; reopen it to make changes.");
        }
    }

    private static void validateDates(ProgramRequestDao request) {
        if (request.getProgramStartDate() != null && request.getProgramEndDate() != null
                && request.getProgramEndDate().isBefore(request.getProgramStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
