package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.model.Role;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.service.EmailService;
import com.shecancode.attendence.registration.Exception.EmailDeliveryException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlertNotificationServiceTest {

    @Mock private AttendanceAlertRepository alertRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;

    @InjectMocks
    private AlertNotificationService notificationService;

    private AppUser trainer;
    private AttendanceAlert consecutive;
    private AttendanceAlert total;

    @BeforeEach
    void setUp() {
        Program program = Program.builder().id(UUID.randomUUID()).programName("Backend").build();
        Cohort cohort = Cohort.builder().id(UUID.randomUUID()).cohortNumber("C1").program(program).build();
        Student student = Student.builder().id(UUID.randomUUID()).studentFirstName("Ada").studentLastName("L")
                .email("ada@x.org").build();
        trainer = AppUser.builder().id(UUID.randomUUID()).username("t@x.org").fullName("Trainer T").role(Role.TRAINER).build();

        consecutive = alert(AlertType.CONSECUTIVE_ABSENCES, student, program, cohort);
        total = alert(AlertType.TOTAL_ABSENCES, student, program, cohort);
        when(alertRepository.findByIdIn(anyCollection())).thenReturn(List.of(consecutive, total));
    }

    private AttendanceAlert alert(AlertType type, Student student, Program program, Cohort cohort) {
        return AttendanceAlert.builder().id(UUID.randomUUID()).alertType(type).status(AlertStatus.ACTIVE)
                .student(student).program(program).cohort(cohort).absenceCount(3)
                .triggeredOnDate(LocalDate.of(2026, 3, 10)).triggeredById(trainer.getId()).build();
    }

    @Test
    void givenTwoAlerts_whenNotified_thenOneEmailEachToStudentAndTrainer() {
        when(userRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));

        notificationService.notifyAlerts(List.of(consecutive.getId(), total.getId()));

        verify(emailService).sendAbsenceAlertToStudent(eq("ada@x.org"), eq("Ada L"), eq("Backend"), eq("C1"),
                eq(List.of("3 absences in a row (as of 2026-03-10)", "3 absences in total (as of 2026-03-10)")));
        verify(emailService).sendAbsenceAlertToTrainer(eq("t@x.org"), eq("Trainer T"), eq("Ada L"), any(), any(), anyList());
        assertNotNull(consecutive.getStudentNotifiedAt());
        assertNotNull(total.getTrainerNotifiedAt());
    }

    @Test
    void givenTrainerEmailFails_whenRetried_thenStudentNotEmailedTwice() {
        when(userRepository.findById(trainer.getId())).thenReturn(Optional.of(trainer));
        doThrow(new EmailDeliveryException("down")).doNothing()
                .when(emailService).sendAbsenceAlertToTrainer(any(), any(), any(), any(), any(), anyList());

        assertThrows(EmailDeliveryException.class,
                () -> notificationService.notifyAlerts(List.of(consecutive.getId(), total.getId())));
        notificationService.notifyAlerts(List.of(consecutive.getId(), total.getId()));

        verify(emailService, times(1)).sendAbsenceAlertToStudent(any(), any(), any(), any(), anyList());
        verify(emailService, times(2)).sendAbsenceAlertToTrainer(any(), any(), any(), any(), any(), anyList());
    }

    @Test
    void givenAlreadyNotified_whenEventRedelivered_thenNoEmails() {
        consecutive.setStudentNotifiedAt(Instant.now());
        consecutive.setTrainerNotifiedAt(Instant.now());
        total.setStudentNotifiedAt(Instant.now());
        total.setTrainerNotifiedAt(Instant.now());

        notificationService.notifyAlerts(List.of(consecutive.getId(), total.getId()));

        verifyNoInteractions(emailService, userRepository);
    }
}
