package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortAlreadyExistException;
import com.shecancode.attendence.registration.Exception.CohortNotFoundException;
import com.shecancode.attendence.registration.Exception.ProgramNotFoundException;
import com.shecancode.attendence.registration.Exception.ReadOnlyException;
import com.shecancode.attendence.registration.Exception.ResourceInUseException;
import com.shecancode.attendence.registration.Mapper.CohortMapper;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.Repository.StudentSpecs;
import com.shecancode.attendence.registration.dao.CohortProgressResponse;
import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class CohortService {

    private final CohortRepository cohortRepository;
    private final ProgramRepository programRepository;
    private final StudentRepository studentRepository;
    private final Clock clock;

    public CohortService(CohortRepository cohortRepository, ProgramRepository programRepository,
                         StudentRepository studentRepository, Clock clock) {
        this.cohortRepository = cohortRepository;
        this.programRepository = programRepository;
        this.studentRepository = studentRepository;
        this.clock = clock;
    }

    public CohortResponseDao createCohort(CohortRequestDao cohortRequestDao) {
        log.info("Starting cohort creation for: {}", LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()));

        if (cohortRequestDao.getCohortNumber() == null || cohortRequestDao.getCohortNumber().isBlank()){
            throw new IllegalArgumentException("Cohort number must not be null ");
        }
        if (cohortRequestDao.getProgramId() == null) {
            throw new IllegalArgumentException("programId is required");
        }

        Program program = programRepository.findByIdAndDeletedAtIsNull(cohortRequestDao.getProgramId())
                .orElseThrow(() -> new ProgramNotFoundException(
                        "Program [" + LoggingUtils.sanitizeForLogging(String.valueOf(cohortRequestDao.getProgramId())) + "] not found."));

        if (program.isClosed()) {
            throw new ReadOnlyException("Program '" + program.getProgramName() + "' is closed; reopen it to add cohorts.");
        }

        if (cohortRepository.existsByProgram_IdAndCohortNumber(cohortRequestDao.getProgramId(), cohortRequestDao.getCohortNumber())) {
            throw new CohortAlreadyExistException("Cohort " + LoggingUtils.sanitizeForLogging(cohortRequestDao.getCohortNumber()) + " already exists in program " + LoggingUtils.sanitizeForLogging(program.getProgramName()));
        }

        validateDates(cohortRequestDao);
        Cohort cohort = Cohort.builder()
                .id(UUID.randomUUID())
                .cohortNumber(cohortRequestDao.getCohortNumber())
                .startDate(cohortRequestDao.getStartDate())
                .endDate(cohortRequestDao.getEndDate())
                .program(program)
                .build();

        Cohort savedCohort = cohortRepository.save(cohort);
        log.info("Cohort created successfully: {}", LoggingUtils.sanitizeForLogging(savedCohort.getCohortNumber()));
        return CohortMapper.toCohortResponseDao(savedCohort);
    }

    /** Both filters are optional. */
    @Transactional(readOnly = true)
    public List<CohortResponseDao> getAllCohorts(UUID programId, LifecycleStatus status) {
        Specification<Cohort> spec = (root, query, cb) -> cb.isNull(root.get("deletedAt"));
        if (programId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("program").get("id"), programId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        return cohortRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "startDate")).stream()
                .map(CohortMapper::toCohortResponseDao)
                .toList();
    }

    @Transactional(readOnly = true)
    public CohortResponseDao getCohortById(UUID cohortId) {
        return CohortMapper.toCohortResponseDao(findCohort(cohortId));
    }

    /** Updates number and dates. The program cannot be changed. */
    public CohortResponseDao updateCohort(UUID cohortId, CohortRequestDao request) {
        Cohort cohort = findCohort(cohortId);
        requireWritable(cohort);
        if (request.getProgramId() != null && !request.getProgramId().equals(cohort.getProgram().getId())) {
            throw new IllegalArgumentException("A cohort cannot be moved to another program");
        }
        if (cohortRepository.existsByProgram_IdAndCohortNumberAndIdNot(
                cohort.getProgram().getId(), request.getCohortNumber(), cohortId)) {
            throw new CohortAlreadyExistException("Cohort " + LoggingUtils.sanitizeForLogging(request.getCohortNumber())
                    + " already exists in program " + LoggingUtils.sanitizeForLogging(cohort.getProgram().getProgramName()));
        }
        validateDates(request);

        cohort.setCohortNumber(request.getCohortNumber());
        cohort.setStartDate(request.getStartDate());
        cohort.setEndDate(request.getEndDate());
        log.info("Cohort [{}] updated", cohortId);
        return CohortMapper.toCohortResponseDao(cohortRepository.save(cohort));
    }

    /** CLOSED makes the cohort read-only. It cannot be reopened while its program is closed. */
    public CohortResponseDao changeStatus(UUID cohortId, LifecycleStatus status) {
        Cohort cohort = findCohort(cohortId);
        if (status == LifecycleStatus.OPEN && cohort.getProgram().isClosed()) {
            throw new ReadOnlyException("Program '" + cohort.getProgram().getProgramName()
                    + "' is closed; reopen the program first.");
        }
        cohort.setStatus(status);
        log.info("Cohort [{}] is now {}", cohortId, status);
        return CohortMapper.toCohortResponseDao(cohortRepository.save(cohort));
    }

    /** Soft delete. Refused while the cohort still has students, so nothing is left orphaned. */
    public void deleteCohort(UUID cohortId) {
        Cohort cohort = findCohort(cohortId);
        requireWritable(cohort);
        long students = studentRepository.countByCohort_IdAndDeletedAtIsNull(cohortId);
        if (students > 0) {
            throw new ResourceInUseException("Cohort still has " + students + " student(s); move or delete them first.");
        }
        cohort.setDeletedAt(Instant.now());
        cohortRepository.save(cohort);
        log.info("Cohort [{}] deleted", cohortId);
    }

    @Transactional(readOnly = true)
    public CohortProgressResponse getProgress(UUID cohortId) {
        Cohort cohort = findCohort(cohortId);
        return progress(cohort.getId(), cohort.getStartDate(), cohort.getEndDate(), LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public List<StudentResponseDao> getParticipants(UUID cohortId, Status status) {
        findCohort(cohortId);
        return studentRepository.findAll(StudentSpecs.matching(null, cohortId, status, null),
                        Sort.by("studentFirstName", "studentLastName")).stream()
                .map(StudentMapper::toDTO)
                .toList();
    }

    static CohortProgressResponse progress(UUID cohortId, LocalDate start, LocalDate end, LocalDate today) {
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        int totalWeeks = (int) Math.ceil(totalDays / 7.0);
        long daysElapsed = Math.min(totalDays, Math.max(0, ChronoUnit.DAYS.between(start, today) + 1));
        long daysRemaining = Math.max(0, ChronoUnit.DAYS.between(today, end));

        int currentWeek;
        if (today.isBefore(start)) currentWeek = 0;
        else if (today.isAfter(end)) currentWeek = totalWeeks;
        else currentWeek = (int) (ChronoUnit.DAYS.between(start, today) / 7) + 1;

        double percentComplete = Math.round(daysElapsed * 1000.0 / totalDays) / 10.0;
        return new CohortProgressResponse(cohortId, start, end, currentWeek, totalWeeks,
                daysElapsed, daysRemaining, percentComplete);
    }

    private Cohort findCohort(UUID cohortId) {
        return cohortRepository.findByIdAndDeletedAtIsNull(cohortId)
                .orElseThrow(() -> new CohortNotFoundException("Cohort [" + cohortId + "] not found."));
    }

    private static void requireWritable(Cohort cohort) {
        if (cohort.isReadOnly()) {
            throw new ReadOnlyException("Cohort " + cohort.getCohortNumber() + " is closed; reopen it to make changes.");
        }
    }

    private static void validateDates(CohortRequestDao request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}
