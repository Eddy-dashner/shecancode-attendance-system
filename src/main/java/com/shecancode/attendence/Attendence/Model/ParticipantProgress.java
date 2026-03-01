package com.shecancode.attendence.Attendence.Model;

import com.shecancode.attendence.Attendence.Enum.ProgressColor;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "participant")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ParticipantProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "attendance_points", nullable = false)
    private double attendancePoints;

    @Column(name = "attendance_percentage", nullable = false)
    private double attendancePercentage;

    @Enumerated(EnumType.STRING)
    @Column(name = "progress_color", nullable = false)
    private ProgressColor color;

    @Column(name = "consecutive_absences", nullable = false)
    private int consecutiveAbsences;

    @Column(name = "last_updated", nullable = false)
    private LocalDate lastUpdated;

    public void updateHealth(double pointsLost, int totalDuration) {
        this.attendancePoints = totalDuration - pointsLost;
        this.attendancePercentage = (this.attendancePoints / totalDuration) * 100;

        if (this.attendancePercentage >= 85) {
            this.color = ProgressColor.GREEN;
        } else if (this.attendancePercentage > 60) {
            this.color = ProgressColor.YELLOW;
        } else {
            this.color = ProgressColor.RED;
        }
        this.lastUpdated = LocalDate.now();
    }

}
