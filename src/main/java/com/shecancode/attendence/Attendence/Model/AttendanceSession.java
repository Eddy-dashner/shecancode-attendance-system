package com.shecancode.attendence.Attendence.Model;

import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One class day for one cohort. Every attendance row belongs to a session, so
 * "sessions held so far" is a simple count and a register can only exist once
 * per cohort per date.
 */
@Entity
@Table(name = "attendance_session", uniqueConstraints = @UniqueConstraint(
        name = "uk_session_cohort_date", columnNames = {"cohort_id", "session_date"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cohort_id", nullable = false)
    private Cohort cohort;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "taken_by_id", nullable = false, updatable = false)
    private UUID takenById;

    @Column(name = "taken_by_name", nullable = false, updatable = false)
    private String takenByName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
