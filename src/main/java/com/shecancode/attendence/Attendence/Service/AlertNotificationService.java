package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.auth.service.EmailService;
import com.shecancode.attendence.registration.Model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Sends the absence-alert emails for alerts raised together in one save: one email
 * to the student and one to the trainer who recorded the triggering day.
 *
 * Deliberately not transactional: each recipient's notified-at stamp is committed
 * as soon as that email is sent, so a retry after a partial failure (e.g. the
 * trainer email fails) never emails the student a second time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertNotificationService {

    private final AttendanceAlertRepository alertRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void notifyAlerts(Collection<UUID> alertIds) {
        List<AttendanceAlert> alerts = alertRepository.findByIdIn(alertIds);
        if (alerts.isEmpty()) {
            log.warn("No alerts found for ids {}; nothing to notify", alertIds);
            return;
        }

        AttendanceAlert first = alerts.get(0);
        Student student = first.getStudent();
        String studentName = AttendanceMapper.studentName(student);
        String programName = first.getProgram().getProgramName();
        String cohortNumber = first.getCohort().getCohortNumber();
        List<String> reasons = alerts.stream().map(AlertNotificationService::describe).toList();

        if (alerts.stream().anyMatch(a -> a.getStudentNotifiedAt() == null)) {
            emailService.sendAbsenceAlertToStudent(student.getEmail(), studentName, programName, cohortNumber, reasons);
            alerts.forEach(a -> a.setStudentNotifiedAt(Instant.now()));
            alertRepository.saveAll(alerts);
        }

        if (alerts.stream().anyMatch(a -> a.getTrainerNotifiedAt() == null)) {
            AppUser trainer = userRepository.findById(first.getTriggeredById()).orElse(null);
            if (trainer == null) {
                log.warn("Trainer {} for alert(s) {} no longer exists; skipping trainer email",
                        first.getTriggeredById(), alertIds);
            } else {
                emailService.sendAbsenceAlertToTrainer(trainer.getUsername(), AttendanceMapper.displayName(trainer),
                        studentName, programName, cohortNumber, reasons);
            }
            alerts.forEach(a -> a.setTrainerNotifiedAt(Instant.now()));
            alertRepository.saveAll(alerts);
        }
    }

    private static String describe(AttendanceAlert alert) {
        String when = alert.getAlertType() == AlertType.CONSECUTIVE_ABSENCES ? "in a row" : "in total";
        return alert.getAbsenceCount() + " absences " + when + " (as of " + alert.getTriggeredOnDate() + ")";
    }
}
