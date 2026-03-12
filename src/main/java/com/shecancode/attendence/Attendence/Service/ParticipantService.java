package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Enum.ProgressColor;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.ParticipantProgress;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Repo.ParticipantProgressRepository;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.util.LoggingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ParticipantService {
    private final AttendanceRepository attendanceRepository;

    private final ParticipantProgressRepository participantProgressRepository;

    public ParticipantService(AttendanceRepository attendanceRepository, ParticipantProgressRepository participantProgressRepository) {
        this.attendanceRepository = attendanceRepository;
        this.participantProgressRepository = participantProgressRepository;
    }


    @Transactional
    public void updateProgress(Student student, Program program) {
        int totalDays = program.getProgramDuration();

        // 1. Calculate Weighted Absences using corrected Repository method names
        // Full Absence = 1.0 point loss
        long fullAbsences = attendanceRepository.countByStudentAndProgramAndAttendanceStatus(
                student, program, AttendanceStatus.ABSENT);

        // Communicated Absence = 0.5 point loss
        long commAbsences = attendanceRepository.countByStudentAndProgramAndAttendanceStatus(
                student, program, AttendanceStatus.ABSENT_COMMUNICATED);

        // 2. Calculate points (Using double for 0.5 precision)
        double pointsLost = (fullAbsences * 1.0) + (commAbsences * 0.5);
        double currentPoints = totalDays - pointsLost;

        // 3. Calculate health percentage
        double percentage = (currentPoints * 100.0) / totalDays;

        // 4. Calculate Consecutive Absences and Color
        int consecutiveAbsences = calculateConsecutiveAbsences(student, program);
        ProgressColor color = determineColor(percentage);

        // 5. Upsert (Update or Insert) the Progress record
        ParticipantProgress progress = participantProgressRepository.findByStudentAndProgram(student, program)
                .orElse(ParticipantProgress.builder()
                        .student(student)
                        .program(program)
                        .lastUpdated(LocalDate.now())
                        .build());

        progress.setAttendancePoints(currentPoints);
        progress.setAttendancePercentage(percentage);
        progress.setColor(color);
        progress.setConsecutiveAbsences(consecutiveAbsences);
        progress.setLastUpdated(LocalDate.now());

        participantProgressRepository.save(progress);

        log.info("Progress updated for student {}: {}% - Color: {}",
                student.getId(), String.format("%.2f", percentage), color);
    }

    private ProgressColor determineColor(double percentage) {
        if (percentage >= 85) {
            return ProgressColor.GREEN;
        } else if (percentage > 60) {
            return ProgressColor.YELLOW;
        } else {
            return ProgressColor.RED;
        }
    }

    public int calculateConsecutiveAbsences(Student student, Program program) {
        // Fetch historical records ordered by date descending (Newest first)
        List<Attendance> records = attendanceRepository
                .findByStudentAndProgramOrderByAttendanceRecordedDateDesc(student, program);

        int count = 0;
        for (Attendance attendance : records) {
            // We count "ABSENT" for the consecutive streak.
            // If you want to include "ABSENT_COMMUNICATED", add it to the IF condition.
            if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT) {
                count++;
            } else {
                // IMPORTANT: Stop counting the moment we find a day they were NOT strictly absent
                break;
            }
        }
        return count;
    }

}
