package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Event.OutboxEvent;
import com.shecancode.attendence.Attendence.Event.OutboxEventFactory;
import com.shecancode.attendence.Attendence.Event.OutboxRepository;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceAlertServiceTest {

    @Mock
    private AttendanceAlertRepository alertRepository;
    @Mock
    private OutboxRepository outboxRepository;
    @Mock
    private OutboxEventFactory outboxEventFactory;

    @InjectMocks
    private AttendanceAlertService alertService;

    private Student student;
    private AttendanceSession session;
    private AppUser trainer;

    @BeforeEach
    void setUp() {
        Program program = Program.builder().id(UUID.randomUUID()).programName("Backend").build();
        Cohort cohort = Cohort.builder().id(UUID.randomUUID()).cohortNumber("C1").program(program).build();
        student = Student.builder().id(UUID.randomUUID()).cohort(cohort).program(program).build();
        session = AttendanceSession.builder().id(UUID.randomUUID()).cohort(cohort).program(program)
                .sessionDate(LocalDate.of(2026, 3, 10)).build();
        trainer = AppUser.builder().id(UUID.randomUUID()).username("t@x.org").fullName("Trainer T").role(Role.TRAINER).build();

        lenient().when(alertRepository.save(any())).thenAnswer(inv -> {
            AttendanceAlert alert = inv.getArgument(0);
            alert.setId(UUID.randomUUID());
            return alert;
        });
        lenient().when(outboxEventFactory.createAlertTriggeredEvent(anyList())).thenReturn(new OutboxEvent());
    }

    private void noActiveAlerts() {
        when(alertRepository.findByStudentIdAndProgramIdAndAlertTypeAndStatus(any(), any(), any(), eq(AlertStatus.ACTIVE)))
                .thenReturn(Optional.empty());
    }

    @Test
    void givenThreeAbsencesInARow_whenEvaluated_thenBothAlertsRaisedInOneEvent() {
        noActiveAlerts();

        List<AttendanceAlert> raised = alertService.evaluate(student, session, new AttendanceScore(3, 3, 0, 0, 3), trainer);

        assertEquals(List.of(AlertType.CONSECUTIVE_ABSENCES, AlertType.TOTAL_ABSENCES),
                raised.stream().map(AttendanceAlert::getAlertType).toList());
        assertEquals(trainer.getId(), raised.get(0).getTriggeredById());
        verify(outboxEventFactory).createAlertTriggeredEvent(raised);
        verify(outboxRepository, times(1)).save(any());
    }

    @Test
    void givenThreeAbsencesNotInARow_whenEvaluated_thenOnlyTotalAlertRaised() {
        noActiveAlerts();

        List<AttendanceAlert> raised = alertService.evaluate(student, session, new AttendanceScore(5, 2, 1, 0, 1), trainer);

        assertEquals(1, raised.size());
        assertEquals(AlertType.TOTAL_ABSENCES, raised.get(0).getAlertType());
        assertEquals(3, raised.get(0).getAbsenceCount());
    }

    @Test
    void givenAlertAlreadyActive_whenEvaluatedAgain_thenNoDuplicateAndNoEmail() {
        when(alertRepository.findByStudentIdAndProgramIdAndAlertTypeAndStatus(any(), any(), any(), eq(AlertStatus.ACTIVE)))
                .thenReturn(Optional.of(AttendanceAlert.builder().status(AlertStatus.ACTIVE).build()));

        List<AttendanceAlert> raised = alertService.evaluate(student, session, new AttendanceScore(4, 4, 0, 0, 4), trainer);

        assertTrue(raised.isEmpty());
        verify(alertRepository, never()).save(any());
        verifyNoInteractions(outboxRepository);
    }

    @Test
    void givenStreakBroken_whenEvaluated_thenConsecutiveAlertResolvedButTotalStays() {
        AttendanceAlert consecutive = AttendanceAlert.builder().alertType(AlertType.CONSECUTIVE_ABSENCES).status(AlertStatus.ACTIVE).build();
        AttendanceAlert total = AttendanceAlert.builder().alertType(AlertType.TOTAL_ABSENCES).status(AlertStatus.ACTIVE).build();
        when(alertRepository.findByStudentIdAndProgramIdAndAlertTypeAndStatus(any(), any(), eq(AlertType.CONSECUTIVE_ABSENCES), any()))
                .thenReturn(Optional.of(consecutive));
        when(alertRepository.findByStudentIdAndProgramIdAndAlertTypeAndStatus(any(), any(), eq(AlertType.TOTAL_ABSENCES), any()))
                .thenReturn(Optional.of(total));

        List<AttendanceAlert> raised = alertService.evaluate(student, session, new AttendanceScore(4, 3, 0, 0, 0), trainer);

        assertTrue(raised.isEmpty());
        assertEquals(AlertStatus.RESOLVED, consecutive.getStatus());
        assertNotNull(consecutive.getResolvedAt());
        assertEquals(AlertStatus.ACTIVE, total.getStatus());
    }

    @Test
    void givenTwoAbsences_whenEvaluated_thenNothingRaised() {
        noActiveAlerts();

        List<AttendanceAlert> raised = alertService.evaluate(student, session, new AttendanceScore(2, 2, 0, 0, 2), trainer);

        assertTrue(raised.isEmpty());
        verifyNoInteractions(outboxRepository);
    }
}
