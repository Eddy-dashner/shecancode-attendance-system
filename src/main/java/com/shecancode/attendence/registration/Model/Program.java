package com.shecancode.attendence.registration.Model;

import jakarta.persistence.*;
import lombok.*;

import com.shecancode.attendence.registration.Enum.LifecycleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PROGRAM")

public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "program_id")
    private UUID id;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @Column(name = "program_Duration", nullable = false)
    private Integer programDuration;

    @Column(name = "program_start_date", nullable = false)
    private LocalDate programStartDate;

    @Column(name = "program_end_date", nullable = false)
    private LocalDate programEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private LifecycleStatus status = LifecycleStatus.OPEN;

    // Soft delete: set when an admin deletes the program; null while it exists.
    @Column(name = "deleted_at")
    private Instant deletedAt;

    public boolean isClosed() {
        return status == LifecycleStatus.CLOSED;
    }

    private boolean isCalendarExpired() {
        return LocalDate.now().isAfter(this.programEndDate);

    }
}
