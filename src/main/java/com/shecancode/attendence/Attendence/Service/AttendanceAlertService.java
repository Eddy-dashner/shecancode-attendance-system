package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.Attendence.Event.OutboxEventFactory;
import com.shecancode.attendence.Attendence.Event.OutboxRepository;
import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.AttendanceAlert;
import com.shecancode.attendence.Attendence.Model.AttendanceSession;
import com.shecancode.attendence.Attendence.Repo.AttendanceAlertRepository;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.registration.Model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps a student's absence alerts in line with their current score. An alert is
 * ACTIVE while its condition holds and RESOLVED once it stops holding, so a new
 * run of absences after a resolved alert raises (and emails) a fresh one.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceAlertService {

    public static final int ABSENCE_THRESHOLD = 3;

    private final AttendanceAlertRepository alertRepository;
    private final OutboxRepository outboxRepository;
    private final OutboxEventFactory outboxEventFactory;

    /**
     * Must run in the same transaction as the attendance save, so the alert and its
     * outbox event commit (or roll back) together with the attendance rows.
     *
     * @return alerts newly raised by this call
     */
    @Transactional
    public List<AttendanceAlert> evaluate(Student student, AttendanceSession session,
                                          AttendanceScore score, AppUser recorder) {
        List<AttendanceAlert> raised = new ArrayList<>();
        sync(AlertType.CONSECUTIVE_ABSENCES, score.consecutiveAbsences(), student, session, recorder)
                .ifPresent(raised::add);
        sync(AlertType.TOTAL_ABSENCES, score.totalAbsences(), student, session, recorder)
                .ifPresent(raised::add);

        if (!raised.isEmpty()) {
            outboxRepository.save(outboxEventFactory.createAlertTriggeredEvent(raised));
        }
        return raised;
    }

    private Optional<AttendanceAlert> sync(AlertType type, int absences, Student student,
                                           AttendanceSession session, AppUser recorder) {
        UUID programId = session.getProgram().getId();
        Optional<AttendanceAlert> active = alertRepository
                .findByStudentIdAndProgramIdAndAlertTypeAndStatus(student.getId(), programId, type, AlertStatus.ACTIVE);

        if (absences < ABSENCE_THRESHOLD) {
            active.ifPresent(alert -> {
                alert.setStatus(AlertStatus.RESOLVED);
                alert.setResolvedAt(Instant.now());
                log.info("Resolved {} alert {} for student {}", type, alert.getId(), student.getId());
            });
            return Optional.empty();
        }

        if (active.isPresent()) {
            return Optional.empty();
        }

        AttendanceAlert alert = alertRepository.save(AttendanceAlert.builder()
                .student(student)
                .program(session.getProgram())
                .cohort(session.getCohort())
                .alertType(type)
                .status(AlertStatus.ACTIVE)
                .absenceCount(absences)
                .triggeredOnDate(session.getSessionDate())
                .triggeredById(recorder.getId())
                .triggeredByName(AttendanceMapper.displayName(recorder))
                .build());
        log.info("Raised {} alert {} for student {} ({} absences)", type, alert.getId(), student.getId(), absences);
        return Optional.of(alert);
    }
}
