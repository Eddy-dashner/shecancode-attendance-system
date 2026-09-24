package com.shecancode.attendence.Attendence.Model;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_session_student", columnNames = {"session_id", "student_id"}),
        indexes = @Index(name = "idx_attendance_date_cohort", columnList = "attendance_recorded_date, cohort_id"))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID attendanceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AttendanceSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    // program, cohort and date duplicate the session's values so history and
    // scoring queries don't need a join; they are always copied from the session.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus;

    // Null for absences.
    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "attendance_recorded_date", nullable = false)
    private LocalDate attendanceRecordedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Whoever saved this row last (taken from the JWT, never from the request body).
    @Column(name = "recorded_by_id", nullable = false)
    private UUID recordedById;

    @Column(name = "recorded_by_name", nullable = false)
    private String recordedByName;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
