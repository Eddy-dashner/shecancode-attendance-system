package com.shecancode.attendence.Attendence.Model;

import com.shecancode.attendence.Attendence.Enum.AlertStatus;
import com.shecancode.attendence.Attendence.Enum.AlertType;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Raised when a student reaches the absence threshold. At most one ACTIVE alert
 * exists per student, program and type; it is RESOLVED once the condition no
 * longer holds (streak broken, or an edit removed absences).
 *
 * Emails are sent asynchronously by the Kafka alert consumer; the notified-at
 * columns make that consumer safe to retry without emailing anyone twice.
 */
@Entity
@Table(name = "attendance_alert", indexes = @Index(
        name = "idx_alert_student_program_status", columnList = "student_id, program_id, status"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "alert_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type", nullable = false)
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AlertStatus status;

    // Absence count at the moment the alert fired.
    @Column(name = "absence_count", nullable = false)
    private int absenceCount;

    @Column(name = "triggered_on_date", nullable = false)
    private LocalDate triggeredOnDate;

    // The trainer/admin whose save crossed the threshold; they get the email.
    @Column(name = "triggered_by_id", nullable = false)
    private UUID triggeredById;

    @Column(name = "triggered_by_name", nullable = false)
    private String triggeredByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "student_notified_at")
    private Instant studentNotifiedAt;

    @Column(name = "trainer_notified_at")
    private Instant trainerNotifiedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}
