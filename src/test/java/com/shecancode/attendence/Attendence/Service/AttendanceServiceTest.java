package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Event.OutboxEvent;
import com.shecancode.attendence.Attendence.Event.OutboxEventFactory;
import com.shecancode.attendence.Attendence.Event.OutboxRepository;
import com.shecancode.attendence.Attendence.Exception.AttendanceEditLockedException;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Repo.AttendanceSessionRepository;
import com.shecancode.attendence.Attendence.Repo.ParticipantProgressRepository;
import com.shecancode.attendence.Attendence.dao.AttendanceEntryRequest;
import com.shecancode.attendence.Attendence.dao.AttendanceRegisterRequest;
import com.shecancode.attendence.Attendence.dao.AttendanceRegisterResponse;
import com.shecancode.attendence.Attendence.dao.SkippedStudent;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceRequestDto;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceSaveResponse;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.CohortProgramMismatchException;
import com.shecancode.attendence.registration.Exception.ResourceNotFoundException;
import com.shecancode.attendence.registration.Exception.StudentDroppedOutException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final ZoneId KIGALI = ZoneId.of("Africa/Kigali");
    private static final LocalDate TODAY = LocalDate.of(2026, 3, 10);

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private AttendanceSessionRepository sessionRepository;
    @Mock private AttendanceAlertRepository alertRepository;
    @Mock private ParticipantProgressRepository progressRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private CohortRepository cohortRepository;
    @Mock private ParticipantService participantService;
    @Mock private AttendanceAlertService alertService;
    @Mock private OutboxRepository outboxRepository;
    @Mock private OutboxEventFactory outboxEventFactory;

    private AttendanceService service;

    private Program program;
    private Cohort cohort;
    private AppUser trainer;
    private AppUser admin;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(TODAY.atTime(10, 0).atZone(KIGALI).toInstant(), KIGALI);
        service = new AttendanceService(attendanceRepository, sessionRepository, alertRepository, progressRepository,
                studentRepository, cohortRepository, participantService, alertService, outboxRepository,
                outboxEventFactory, clock);

        program = Program.builder().id(UUID.randomUUID()).programName("Backend").build();
        cohort = Cohort.builder().id(UUID.randomUUID()).cohortNumber("C1").program(program)
                .startDate(LocalDate.of(2026, 3, 1)).endDate(LocalDate.of(2026, 9, 1)).build();
        trainer = AppUser.builder().id(UUID.randomUUID()).username("t@x.org").fullName("Trainer T").role(Role.TRAINER).build();
        admin = AppUser.builder().id(UUID.randomUUID()).username("a@x.org").fullName("Admin A").role(Role.ADMIN).build();

        lenient().when(cohortRepository.findById(cohort.getId())).thenReturn(Optional.of(cohort));
        lenient().when(sessionRepository.saveAndFlush(any())).thenAnswer(inv -> {
            AttendanceSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });
        lenient().when(outboxEventFactory.createAttendanceOutboxEvent(any())).thenReturn(new OutboxEvent());
        lenient().when(outboxEventFactory.createAttendanceUpdatedEvent(any())).thenReturn(new OutboxEvent());
        lenient().when(participantService.updateProgress(any(), any())).thenReturn(AttendanceScore.of(List.of()));
    }

    private Student student(Status status, Cohort studentCohort) {
        return Student.builder().id(UUID.randomUUID()).studentFirstName("Ada").studentLastName("L")
                .email(UUID.randomUUID() + "@x.org").status(status)
                .cohort(studentCohort).program(studentCohort.getProgram()).build();
    }

    private static StudentAttendanceRequestDto entry(Student s, AttendanceStatus status) {
        return StudentAttendanceRequestDto.builder().studentId(s.getId()).attendanceStatus(status)
                .checkInTime(status.requiresCheckInTime() ? LocalTime.of(9, 0) : null).build();
    }

    private static AttendanceRegisterRequest register(StudentAttendanceRequestDto... entries) {
        return new AttendanceRegisterRequest(List.of(entries));
    }

    @Test
    void givenTrainer_whenSavingYesterday_thenLocked() {
        Student s = student(Status.ACTIVE, cohort);

        assertThrows(AttendanceEditLockedException.class, () -> service.saveRegister(
                program.getId(), cohort.getId(), TODAY.minusDays(1), register(entry(s, AttendanceStatus.PRESENT)), trainer));
        verifyNoInteractions(attendanceRepository);
    }

    @Test
    void givenAdmin_whenSavingYesterday_thenSaved() {
        Student s = student(Status.ACTIVE, cohort);
        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY.minusDays(1))).thenReturn(Optional.empty());
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        AttendanceRegisterResponse response = service.saveRegister(
                program.getId(), cohort.getId(), TODAY.minusDays(1), register(entry(s, AttendanceStatus.PRESENT)), admin);

        assertEquals(1, response.getRecords().size());
        assertEquals("Admin A", response.getTakenByName());
    }

    @Test
    void givenFutureDate_whenSaving_thenRejected() {
        Student s = student(Status.ACTIVE, cohort);

        assertThrows(IllegalArgumentException.class, () -> service.saveRegister(
                program.getId(), cohort.getId(), TODAY.plusDays(1), register(entry(s, AttendanceStatus.PRESENT)), admin));
    }

    @Test
    void givenCohortFromAnotherProgram_whenSaving_thenMismatch() {
        Student s = student(Status.ACTIVE, cohort);

        assertThrows(CohortProgramMismatchException.class, () -> service.saveRegister(
                UUID.randomUUID(), cohort.getId(), TODAY, register(entry(s, AttendanceStatus.PRESENT)), trainer));
    }

    @Test
    void givenPresentWithoutCheckInTime_whenSaving_thenRejected() {
        Student s = student(Status.ACTIVE, cohort);
        StudentAttendanceRequestDto dto = StudentAttendanceRequestDto.builder()
                .studentId(s.getId()).attendanceStatus(AttendanceStatus.PRESENT).build();

        assertThrows(IllegalArgumentException.class, () -> service.saveRegister(
                program.getId(), cohort.getId(), TODAY, register(dto), trainer));
    }

    @Test
    void givenSameStudentTwice_whenSaving_thenRejected() {
        Student s = student(Status.ACTIVE, cohort);

        assertThrows(IllegalArgumentException.class, () -> service.saveRegister(program.getId(), cohort.getId(), TODAY,
                register(entry(s, AttendanceStatus.PRESENT), entry(s, AttendanceStatus.ABSENT)), trainer));
    }

    @Test
    void givenMixedStudents_whenSaving_thenInvalidOnesSkippedAndValidOneSavedAndScored() {
        Student active = student(Status.ACTIVE, cohort);
        Student dropped = student(Status.DROPPED_OUT, cohort);
        Cohort otherCohort = Cohort.builder().id(UUID.randomUUID()).program(program).build();
        Student elsewhere = student(Status.ACTIVE, otherCohort);
        UUID unknownId = UUID.randomUUID();
        StudentAttendanceRequestDto unknown = StudentAttendanceRequestDto.builder()
                .studentId(unknownId).attendanceStatus(AttendanceStatus.ABSENT).build();

        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.empty());
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(active, dropped, elsewhere));

        AttendanceRegisterResponse response = service.saveRegister(program.getId(), cohort.getId(), TODAY,
                register(entry(active, AttendanceStatus.ABSENT), entry(dropped, AttendanceStatus.PRESENT),
                        entry(elsewhere, AttendanceStatus.PRESENT), unknown), trainer);

        assertEquals(1, response.getRecords().size());
        assertNull(response.getRecords().get(0).getCheckInTime());
        assertEquals(trainer.getId(), response.getRecords().get(0).getRecordedById());
        assertEquals(List.of(
                new SkippedStudent(dropped.getId(), SkippedStudent.Reason.DROPPED_OUT),
                new SkippedStudent(elsewhere.getId(), SkippedStudent.Reason.NOT_IN_COHORT),
                new SkippedStudent(unknownId, SkippedStudent.Reason.NOT_FOUND)), response.getSkipped());
        verify(participantService).updateProgress(active, program);
        verify(alertService).evaluate(eq(active), any(), any(), eq(trainer));
    }

    @Test
    void givenUnchangedResubmission_whenSaving_thenNoEventsAndNoRescore() {
        Student s = student(Status.ACTIVE, cohort);
        AttendanceSession session = AttendanceSession.builder().id(UUID.randomUUID()).cohort(cohort).program(program)
                .sessionDate(TODAY).takenByName("Trainer T").build();
        Attendance existing = Attendance.builder().attendanceId(UUID.randomUUID()).session(session).student(s)
                .program(program).cohort(cohort).attendanceRecordedDate(TODAY)
                .attendanceStatus(AttendanceStatus.PRESENT).checkInTime(LocalTime.of(9, 0)).build();

        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionId(session.getId())).thenReturn(List.of(existing));
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        service.saveRegister(program.getId(), cohort.getId(), TODAY, register(entry(s, AttendanceStatus.PRESENT)), trainer);

        verify(outboxRepository).saveAll(List.of());
        verifyNoInteractions(participantService, alertService);
        verify(sessionRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenRemarksOnlyChange_whenSaving_thenUpdatedEventButNoRescore() {
        Student s = student(Status.ACTIVE, cohort);
        AttendanceSession session = AttendanceSession.builder().id(UUID.randomUUID()).cohort(cohort).program(program)
                .sessionDate(TODAY).build();
        Attendance existing = Attendance.builder().attendanceId(UUID.randomUUID()).session(session).student(s)
                .program(program).cohort(cohort).attendanceRecordedDate(TODAY)
                .attendanceStatus(AttendanceStatus.PRESENT).checkInTime(LocalTime.of(9, 0)).build();
        StudentAttendanceRequestDto dto = entry(s, AttendanceStatus.PRESENT);
        dto.setRemarks("Left early");

        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionId(session.getId())).thenReturn(List.of(existing));
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        service.saveRegister(program.getId(), cohort.getId(), TODAY, register(dto), trainer);

        assertEquals("Left early", existing.getRemarks());
        verify(outboxEventFactory).createAttendanceUpdatedEvent(existing);
        verifyNoInteractions(participantService, alertService);
    }

    @Test
    void givenSingleStudentWithoutStatus_whenSaving_thenRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.saveStudentAttendance(program.getId(), cohort.getId(),
                TODAY, UUID.randomUUID(), new AttendanceEntryRequest(), trainer));
    }

    @Test
    void givenDroppedOutStudent_whenSavingSingle_thenConflictInsteadOfSkip() {
        Student dropped = student(Status.DROPPED_OUT, cohort);
        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.empty());
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(dropped));

        assertThrows(StudentDroppedOutException.class, () -> service.saveStudentAttendance(program.getId(), cohort.getId(),
                TODAY, dropped.getId(), AttendanceEntryRequest.builder().attendanceStatus(AttendanceStatus.ABSENT).build(), trainer));
    }

    @Test
    void givenSingleStudent_whenSaving_thenRecordReturned() {
        Student s = student(Status.ACTIVE, cohort);
        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.empty());
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        StudentAttendanceSaveResponse response = service.saveStudentAttendance(program.getId(), cohort.getId(), TODAY,
                s.getId(), AttendanceEntryRequest.builder().attendanceStatus(AttendanceStatus.LATE_PRESENT)
                        .checkInTime(LocalTime.of(9, 20)).build(), trainer);

        assertEquals("LATE_PRESENT", response.getRecord().getAttendanceStatus());
        assertEquals(s.getId(), response.getRecord().getStudentId());
    }

    @Test
    void givenPartialUpdate_whenUpdatingById_thenOnlySentFieldsChangeAndStudentRescored() {
        Student s = student(Status.ACTIVE, cohort);
        AttendanceSession session = AttendanceSession.builder().id(UUID.randomUUID()).cohort(cohort).program(program)
                .sessionDate(TODAY).build();
        Attendance existing = Attendance.builder().attendanceId(UUID.randomUUID()).session(session).student(s)
                .program(program).cohort(cohort).attendanceRecordedDate(TODAY)
                .attendanceStatus(AttendanceStatus.PRESENT).checkInTime(LocalTime.of(9, 0)).remarks("On time").build();
        when(attendanceRepository.findById(existing.getAttendanceId())).thenReturn(Optional.of(existing));
        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionId(session.getId())).thenReturn(List.of(existing));
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        service.updateAttendance(existing.getAttendanceId(),
                AttendanceEntryRequest.builder().attendanceStatus(AttendanceStatus.LATE_PRESENT).build(), trainer);

        assertEquals(AttendanceStatus.LATE_PRESENT, existing.getAttendanceStatus());
        assertEquals(LocalTime.of(9, 0), existing.getCheckInTime());
        assertEquals("On time", existing.getRemarks());
        verify(participantService).updateProgress(s, program);
    }

    @Test
    void givenAbsentRecordWithNoCheckIn_whenUpdatingRemarksOnly_thenSaved() {
        Student s = student(Status.ACTIVE, cohort);
        AttendanceSession session = AttendanceSession.builder().id(UUID.randomUUID()).cohort(cohort).program(program)
                .sessionDate(TODAY).build();
        Attendance existing = Attendance.builder().attendanceId(UUID.randomUUID()).session(session).student(s)
                .program(program).cohort(cohort).attendanceRecordedDate(TODAY)
                .attendanceStatus(AttendanceStatus.ABSENT).build();
        when(attendanceRepository.findById(existing.getAttendanceId())).thenReturn(Optional.of(existing));
        when(sessionRepository.findByCohortIdAndSessionDate(cohort.getId(), TODAY)).thenReturn(Optional.of(session));
        when(attendanceRepository.findBySessionId(session.getId())).thenReturn(List.of(existing));
        when(studentRepository.findAllById(anyList())).thenReturn(List.of(s));

        service.updateAttendance(existing.getAttendanceId(), AttendanceEntryRequest.builder().remarks("Sick").build(), trainer);

        assertEquals("Sick", existing.getRemarks());
        assertEquals(AttendanceStatus.ABSENT, existing.getAttendanceStatus());
        verifyNoInteractions(participantService);
    }

    @Test
    void givenYesterdaysRecord_whenTrainerUpdatesById_thenLocked() {
        Student s = student(Status.ACTIVE, cohort);
        Attendance existing = Attendance.builder().attendanceId(UUID.randomUUID()).student(s).program(program)
                .cohort(cohort).attendanceRecordedDate(TODAY.minusDays(1)).attendanceStatus(AttendanceStatus.ABSENT).build();
        when(attendanceRepository.findById(existing.getAttendanceId())).thenReturn(Optional.of(existing));

        assertThrows(AttendanceEditLockedException.class, () -> service.updateAttendance(existing.getAttendanceId(),
                AttendanceEntryRequest.builder().attendanceStatus(AttendanceStatus.PRESENT).checkInTime(LocalTime.of(9, 0)).build(),
                trainer));
    }

    @Test
    void givenUnknownId_whenUpdating_thenNotFound() {
        when(attendanceRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateAttendance(UUID.randomUUID(),
                new AttendanceEntryRequest(), admin));
    }
}
