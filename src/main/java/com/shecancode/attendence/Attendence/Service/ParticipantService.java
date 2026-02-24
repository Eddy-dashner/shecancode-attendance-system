//package com.shecancode.attendence.Attendence.Service;
//
//import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
//import com.shecancode.attendence.Attendence.Enum.ProgressColor;
//import com.shecancode.attendence.Attendence.Model.Attendance;
//import com.shecancode.attendence.Attendence.Model.ParticipantProgress;
//import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
//import com.shecancode.attendence.Attendence.Repo.ParticipantProgressRepository;
//import com.shecancode.attendence.registration.Model.Program;
//import com.shecancode.attendence.registration.Model.Student;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Slf4j
//@Service
//public class ParticipantService {
//    private final AttendanceRepository attendanceRepository;
//
//    private final ParticipantProgressRepository participantProgressRepository;
//
//    public ParticipantService(AttendanceRepository attendanceRepository, ParticipantProgressRepository participantProgressRepository) {
//        this.attendanceRepository = attendanceRepository;
//        this.participantProgressRepository = participantProgressRepository;
//    }
//
//
//    public void updateProgress(Student student, Program program){
//
//        int totalDays = program.getProgramDuration();
//
//        //counting absent.
//
//        int absences = attendanceRepository.countByStudentAndProgramAndAttendanceStatus(student,program, AttendanceStatus.ABSENT);
//
//        int points = totalDays-absences;
//
//        double percentage = (points * 100.0)/totalDays;
//
//        int consecutiveAbsences = calculateConsecutiveAbsences(student, program);
//        ProgressColor color = determineColor(percentage);
//
//        ParticipantProgress progress = participantProgressRepository.findByStudentAndProgram(student, program)
//                .orElse(ParticipantProgress.builder()
//                        .student(student)
//                        .program(program)
//                        .build());
//        progress.setAttendancePoints(points);
//        progress.setAttendancePercentage(percentage);
//        progress.setColor(color);
//        progress.setConsecutiveAbsences(consecutiveAbsences);
//        progress.setLastUpdated(LocalDate.now());
//
//       participantProgressRepository.save(progress);
//
//        log.info("Participant progress updated for student {}", student.getId());
//    }
//
//    private ProgressColor determineColor(double percentage){
//
//        if (percentage >= 85){
//            return ProgressColor.GREEN;
//        }else if (percentage >60){
//            return ProgressColor.YELLOW;
//
//        } else {
//            return ProgressColor.RED;
//        }
//    }
//
//    public int calculateConsecutiveAbsences(Student student, Program program){
//        List<Attendance> records = attendanceRepository.findByStudentAndProgramOrderByAttendanceRecordedDateDesc(student, program);
//
//        int count = 0;
//
//        for (Attendance attendance: records){
//            if (attendance.getAttendanceStatus() == AttendanceStatus.ABSENT){
//                count++;
//            }break;
//        }
//        return count;
//    }
//
//}
