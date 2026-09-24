package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Event.OutboxEvent;
import com.shecancode.attendence.Attendence.Event.OutboxEventFactory;
import com.shecancode.attendence.Attendence.Event.OutboxRepository;
import com.shecancode.attendence.Attendence.Exception.AttendanceConflictException;
import com.shecancode.attendence.Attendence.Exception.AttendanceEditLockedException;
import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import com.shecancode.attendence.Attendence.Model.ParticipantProgress;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Repo.AttendanceSessionRepository;
import com.shecancode.attendence.Attendence.Repo.ParticipantProgressRepository;
import com.shecancode.attendence.Attendence.dao.*;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortProgramMismatchException;
import com.shecancode.attendence.registration.Exception.ResourceNotFoundException;
import com.shecancode.attendence.registration.Exception.StudentDroppedOutException;
import com.shecancode.attendence.registration.Exception.StudentNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceAlertRepository alertRepository;
    private final ParticipantProgressRepository progressRepository;
    private final StudentRepository studentRepository;
    private final CohortRepository cohortRepository;
    private final ParticipantService participantService;
    private final AttendanceAlertService alertService;
    private final OutboxRepository outboxRepository;
    private final OutboxEventFactory outboxEventFactory;
    private final Clock clock;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             AttendanceSessionRepository sessionRepository,
                             AttendanceAlertRepository alertRepository,
                             ParticipantProgressRepository progressRepository,
                             StudentRepository studentRepository,
                             CohortRepository cohortRepository,
                             ParticipantService participantService,
                             AttendanceAlertService alertService,
                             OutboxRepository outboxRepository,
                             OutboxEventFactory outboxEventFactory,
                             Clock clock) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.alertRepository = alertRepository;
        this.progressRepository = progressRepository;
        this.studentRepository = studentRepository;
        this.cohortRepository = cohortRepository;
        this.participantService = participantService;
        this.alertService = alertService;
        this.outboxRepository = outboxRepository;
        this.outboxEventFactory = outboxEventFactory;
        this.clock = clock;
    }

    /**
     * Creates or updates the register for one cohort on one date. Idempotent: saving
     * the same register twice changes nothing. Students who are unknown, in another
     * cohort or dropped out are skipped and reported rather than failing the request.
     */
    @Transactional
    public AttendanceRegisterResponse saveRegister(UUID programId, UUID cohortId, LocalDate date,
                                                   AttendanceRegisterRequest request, AppUser caller) {
        Cohort cohort = loadCohort(programId, cohortId);
        LocalDate today = LocalDate.now(clock);
        validateEntries(request.getStudents());
        validateDate(cohort, date, today);
        if (caller.getRole() == Role.TRAINER && !date.equals(today)) {
            throw new AttendanceEditLockedException(
                    "Trainers can only take or change attendance for today (" + today + "). Ask an admin to change " + date + ".");
        }

        Optional<AttendanceSession> existingSession = sessionRepository.findByCohortIdAndSessionDate(cohortId, date);
        AttendanceSession session = existingSession.orElseGet(() -> AttendanceSession.builder()
                .cohort(cohort)
                .program(cohort.getProgram())
                .sessionDate(date)
                .takenById(caller.getId())
                .takenByName(AttendanceMapper.displayName(caller))
                .build());

        Map<UUID, Attendance> existingRecords = existingSession.isEmpty()
                ? Map.of()
                : attendanceRepository.findBySessionId(session.getId()).stream()
                        .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity()));

        Map<UUID, Student> students = studentRepository.findAllById(
                        request.getStudents().stream().map(StudentAttendanceRequestDto::getStudentId).toList())
                .stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));

        List<Attendance> records = new ArrayList<>();
        List<Attendance> created = new ArrayList<>();
        List<Attendance> updated = new ArrayList<>();
        // Only a new or changed status can move the score or trip/clear an alert.
        List<Attendance> rescore = new ArrayList<>();
        List<SkippedStudent> skipped = new ArrayList<>();

        for (StudentAttendanceRequestDto dto : request.getStudents()) {
            Student student = students.get(dto.getStudentId());
            SkippedStudent.Reason skipReason = skipReason(student, programId, cohortId);
            if (skipReason != null) {
                skipped.add(new SkippedStudent(dto.getStudentId(), skipReason));
                continue;
            }

            Attendance record = existingRecords.get(student.getId());
            if (record == null) {
                record = AttendanceMapper.toAttendance(dto, student, session, caller);
                created.add(record);
                rescore.add(record);
            } else if (isChanged(record, dto)) {
                if (record.getAttendanceStatus() != dto.getAttendanceStatus()) rescore.add(record);
                AttendanceMapper.applyEntry(record, dto, caller);
                updated.add(record);
            }
            records.add(record);
        }

        try {
            if (existingSession.isEmpty()) {
                session = sessionRepository.saveAndFlush(session);
            }
            attendanceRepository.saveAllAndFlush(records);
        } catch (DataIntegrityViolationException e) {
            throw new AttendanceConflictException(
                    "Attendance for this cohort and date was saved by someone else at the same time. Reload and try again.", e);
        }

        List<OutboxEvent> events = new ArrayList<>();
        created.forEach(a -> events.add(outboxEventFactory.createAttendanceOutboxEvent(a)));
        updated.forEach(a -> events.add(outboxEventFactory.createAttendanceUpdatedEvent(a)));
        outboxRepository.saveAll(events);

        List<AttendanceAlert> alertsRaised = new ArrayList<>();
        for (Attendance record : rescore) {
            AttendanceScore score = participantService.updateProgress(record.getStudent(), session.getProgram());
            alertsRaised.addAll(alertService.evaluate(record.getStudent(), session, score, caller));
        }

        log.info("Register saved for cohort {} on {} by {}: {} new, {} updated, {} skipped, {} alert(s)",
                cohortId, date, caller.getUsername(), created.size(), updated.size(), skipped.size(), alertsRaised.size());

        return AttendanceRegisterResponse.builder()
                .sessionId(session.getId())
                .programId(programId)
                .cohortId(cohortId)
                .sessionDate(date)
                .takenByName(session.getTakenByName())
                .records(records.stream().map(a -> AttendanceMapper.toResponseDTO(a, today)).toList())
                .skipped(skipped)
                .alertsRaised(alertsRaised.stream().map(AttendanceMapper::toAlertResponse).toList())
                .build();
    }

    /**
     * Records (or re-records) one student's attendance on a date. Same rules as the
     * full register, but an ineligible student is an error rather than a skip.
     */
    @Transactional
    public StudentAttendanceSaveResponse saveStudentAttendance(UUID programId, UUID cohortId, LocalDate date,
                                                               UUID studentId, AttendanceEntryRequest request,
                                                               AppUser caller) {
        if (request.getAttendanceStatus() == null) {
            throw new IllegalArgumentException("attendanceStatus is required");
        }
        return saveOne(programId, cohortId, date,
                toEntry(studentId, request.getAttendanceStatus(), request.getCheckInTime(), request.getRemarks()),
                caller);
    }

    /**
     * Partially updates one attendance record: fields left null keep their current
     * value. The trainer same-day rule applies to the record's date.
     */
    @Transactional
    public StudentAttendanceSaveResponse updateAttendance(UUID attendanceId, AttendanceEntryRequest request,
                                                          AppUser caller) {
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found: " + attendanceId));

        StudentAttendanceRequestDto entry = toEntry(
                attendance.getStudent().getId(),
                request.getAttendanceStatus() != null ? request.getAttendanceStatus() : attendance.getAttendanceStatus(),
                request.getCheckInTime() != null ? request.getCheckInTime() : attendance.getCheckInTime(),
                request.getRemarks() != null ? request.getRemarks() : attendance.getRemarks());

        return saveOne(attendance.getProgram().getId(), attendance.getCohort().getId(),
                attendance.getAttendanceRecordedDate(), entry, caller);
    }

    private StudentAttendanceSaveResponse saveOne(UUID programId, UUID cohortId, LocalDate date,
                                                  StudentAttendanceRequestDto entry, AppUser caller) {
        AttendanceRegisterResponse result = saveRegister(
                programId, cohortId, date, new AttendanceRegisterRequest(List.of(entry)), caller);

        if (!result.getSkipped().isEmpty()) {
            UUID studentId = entry.getStudentId();
            switch (result.getSkipped().get(0).reason()) {
                case NOT_FOUND -> throw new StudentNotFoundException("Student not found: " + studentId);
                case NOT_IN_COHORT -> throw new StudentNotFoundException("Student " + studentId + " is not in this cohort");
                case DROPPED_OUT -> throw new StudentDroppedOutException("Student " + studentId + " has dropped out");
            }
        }
        return StudentAttendanceSaveResponse.builder()
                .record(result.getRecords().get(0))
                .alertsRaised(result.getAlertsRaised())
                .build();
    }

    private static StudentAttendanceRequestDto toEntry(UUID studentId, AttendanceStatus status,
                                                       LocalTime checkInTime, String remarks) {
        return StudentAttendanceRequestDto.builder()
                .studentId(studentId)
                .attendanceStatus(status)
                .checkInTime(checkInTime)
                .remarks(remarks)
                .build();
    }

    @Transactional(readOnly = true)
    public CohortRegisterResponse getRegister(UUID programId, UUID cohortId, LocalDate date) {
        loadCohort(programId, cohortId);
        Optional<AttendanceSession> session = sessionRepository.findByCohortIdAndSessionDate(cohortId, date);

        Map<UUID, Attendance> records = session
                .map(s -> attendanceRepository.findBySessionId(s.getId()).stream()
                        .collect(Collectors.toMap(a -> a.getStudent().getId(), Function.identity())))
                .orElse(Map.of());

        List<CohortRegisterResponse.Entry> entries = enrolledStudents(cohortId).stream()
                .map(student -> {
                    Attendance a = records.get(student.getId());
                    return new CohortRegisterResponse.Entry(
                            student.getId(),
                            AttendanceMapper.studentName(student),
                            a == null ? null : a.getAttendanceId(),
                            a == null ? null : a.getAttendanceStatus(),
                            a == null ? null : a.getCheckInTime(),
                            a == null ? null : a.getRemarks());
                })
                .toList();

        return CohortRegisterResponse.builder()
                .sessionId(session.map(AttendanceSession::getId).orElse(null))
                .programId(programId)
                .cohortId(cohortId)
                .sessionDate(date)
                .takenByName(session.map(AttendanceSession::getTakenByName).orElse(null))
                .entries(entries)
                .build();
    }

    @Transactional(readOnly = true)
    public CohortAttendanceSummaryResponse getCohortSummary(UUID programId, UUID cohortId) {
        Cohort cohort = loadCohort(programId, cohortId);
        List<Student> students = enrolledStudents(cohortId);

        Map<UUID, ParticipantProgress> progress = progressRepository
                .findByProgramIdAndStudentIdIn(programId, students.stream().map(Student::getId).toList())
                .stream()
                .collect(Collectors.toMap(p -> p.getStudent().getId(), Function.identity()));

        Map<UUID, List<AlertType>> activeAlerts = alertRepository
                .findByCohortIdAndStatusOrderByCreatedAtDesc(cohortId, AlertStatus.ACTIVE).stream()
                .collect(Collectors.groupingBy(a -> a.getStudent().getId(),
                        Collectors.mapping(AttendanceAlert::getAlertType, Collectors.toList())));

        List<CohortAttendanceSummaryResponse.StudentSummary> rows = students.stream()
                .map(student -> {
                    ParticipantProgress p = progress.get(student.getId());
                    return new CohortAttendanceSummaryResponse.StudentSummary(
                            student.getId(),
                            AttendanceMapper.studentName(student),
                            p == null ? null : p.getAttendancePoints(),
                            p == null ? null : p.getAttendancePercentage(),
                            p == null ? null : p.getColor(),
                            p == null ? null : p.getConsecutiveAbsences(),
                            activeAlerts.getOrDefault(student.getId(), List.of()));
                })
                .toList();

        return CohortAttendanceSummaryResponse.builder()
                .programId(programId)
                .cohortId(cohortId)
                .cohortNumber(cohort.getCohortNumber())
                .students(rows)
                .build();
    }

    @Transactional(readOnly = true)
    public StudentAttendanceHistoryResponse getStudentHistory(UUID programId, UUID cohortId, UUID studentId) {
        loadCohort(programId, cohortId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
        if (student.getCohort() == null || !student.getCohort().getId().equals(cohortId)) {
            throw new StudentNotFoundException("Student " + studentId + " is not in this cohort");
        }
        return buildHistory(student);
    }

    /** The logged-in student's own attendance. */
    @Transactional(readOnly = true)
    public StudentAttendanceHistoryResponse getMyHistory(String username) {
        Student student = studentRepository.findByEmail(username)
                .orElseThrow(() -> new StudentNotFoundException("No student record for this account"));
        if (student.getProgram() == null) {
            throw new StudentNotFoundException("This student is not enrolled in a program");
        }
        return buildHistory(student);
    }

    @Transactional(readOnly = true)
    public List<AttendanceAlertResponse> getAlerts(UUID cohortId, AlertStatus status) {
        List<AttendanceAlert> alerts = cohortId == null
                ? alertRepository.findByStatusOrderByCreatedAtDesc(status)
                : alertRepository.findByCohortIdAndStatusOrderByCreatedAtDesc(cohortId, status);
        return alerts.stream().map(AttendanceMapper::toAlertResponse).toList();
    }

    private StudentAttendanceHistoryResponse buildHistory(Student student) {
        UUID programId = student.getProgram().getId();
        LocalDate today = LocalDate.now(clock);
        List<Attendance> history = attendanceRepository.findHistory(student.getId(), programId);
        AttendanceScore score = AttendanceScore.of(history.stream().map(Attendance::getAttendanceStatus).toList());

        return StudentAttendanceHistoryResponse.builder()
                .studentId(student.getId())
                .studentName(AttendanceMapper.studentName(student))
                .programId(programId)
                .cohortId(student.getCohort() == null ? null : student.getCohort().getId())
                .sessionsRecorded(score.sessionsRecorded())
                .absent(score.absent())
                .absentCommunicated(score.absentCommunicated())
                .late(score.late())
                .consecutiveAbsences(score.consecutiveAbsences())
                .attendancePoints(score.pointsKept())
                .attendancePercentage(score.percentage())
                .color(score.color())
                .records(history.stream().map(a -> AttendanceMapper.toResponseDTO(a, today)).toList())
                .build();
    }

    private Cohort loadCohort(UUID programId, UUID cohortId) {
        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found: " + cohortId));
        if (!cohort.getProgram().getId().equals(programId)) {
            throw new CohortProgramMismatchException("Cohort " + cohortId + " does not belong to program " + programId);
        }
        return cohort;
    }

    private List<Student> enrolledStudents(UUID cohortId) {
        return studentRepository.findByCohort_IdOrderByStudentFirstNameAscStudentLastNameAsc(cohortId).stream()
                .filter(s -> s.getStatus() != Status.DROPPED_OUT)
                .toList();
    }

    private static void validateDate(Cohort cohort, LocalDate date, LocalDate today) {
        if (date.isAfter(today)) {
            throw new IllegalArgumentException("Cannot take attendance for a future date (" + date + ")");
        }
        if (date.isBefore(cohort.getStartDate()) || date.isAfter(cohort.getEndDate())) {
            throw new IllegalArgumentException("Date " + date + " is outside the cohort's dates ("
                    + cohort.getStartDate() + " to " + cohort.getEndDate() + ")");
        }
    }

    private static void validateEntries(List<StudentAttendanceRequestDto> entries) {
        Set<UUID> seen = new HashSet<>();
        for (StudentAttendanceRequestDto dto : entries) {
            if (!seen.add(dto.getStudentId())) {
                throw new IllegalArgumentException("Student " + dto.getStudentId() + " appears more than once");
            }
            if (dto.getAttendanceStatus().requiresCheckInTime() && dto.getCheckInTime() == null) {
                throw new IllegalArgumentException("checkInTime is required for " + dto.getAttendanceStatus()
                        + " (student " + dto.getStudentId() + ")");
            }
        }
    }

    private static SkippedStudent.Reason skipReason(Student student, UUID programId, UUID cohortId) {
        if (student == null) return SkippedStudent.Reason.NOT_FOUND;
        if (student.getStatus() == Status.DROPPED_OUT) return SkippedStudent.Reason.DROPPED_OUT;
        boolean inCohort = student.getCohort() != null && student.getCohort().getId().equals(cohortId)
                && student.getProgram() != null && student.getProgram().getId().equals(programId);
        return inCohort ? null : SkippedStudent.Reason.NOT_IN_COHORT;
    }

    private static boolean isChanged(Attendance record, StudentAttendanceRequestDto dto) {
        Object newCheckIn = dto.getAttendanceStatus().requiresCheckInTime() ? dto.getCheckInTime() : null;
        return record.getAttendanceStatus() != dto.getAttendanceStatus()
                || !Objects.equals(record.getCheckInTime(), newCheckIn)
                || !Objects.equals(record.getRemarks(), dto.getRemarks());
    }
}
