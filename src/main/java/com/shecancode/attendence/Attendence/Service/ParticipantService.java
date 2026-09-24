package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Model.ParticipantProgress;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Repo.ParticipantProgressRepository;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
public class ParticipantService {
    private final AttendanceRepository attendanceRepository;

    private final ParticipantProgressRepository participantProgressRepository;

    public ParticipantService(AttendanceRepository attendanceRepository, ParticipantProgressRepository participantProgressRepository) {
        this.attendanceRepository = attendanceRepository;
        this.participantProgressRepository = participantProgressRepository;
    }

    /**
     * Recalculates the student's stored progress from their full attendance history
     * and returns the score, which the caller uses to evaluate alerts.
     */
    @Transactional
    public AttendanceScore updateProgress(Student student, Program program) {
        AttendanceScore score = AttendanceScore.of(
                attendanceRepository.findHistory(student.getId(), program.getId()).stream()
                        .map(Attendance::getAttendanceStatus)
                        .toList());

        ParticipantProgress progress = participantProgressRepository.findByStudentAndProgram(student, program)
                .orElseGet(() -> ParticipantProgress.builder()
                        .student(student)
                        .program(program)
                        .build());

        progress.setAttendancePoints(score.pointsKept());
        progress.setAttendancePercentage(score.percentage());
        progress.setColor(score.color());
        progress.setConsecutiveAbsences(score.consecutiveAbsences());
        progress.setLastUpdated(LocalDate.now());

        participantProgressRepository.save(progress);

        log.info("Progress updated for student {}: {}% - Color: {}",
                student.getId(), String.format("%.2f", score.percentage()), score.color());
        return score;
    }
}
