package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Enum.AttendanceStatus;
import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.dto.AttendanceRequest;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private  final  StudentRepository studentRepository;
    private final ProgramRepository programRepository;
    private final CohortRepository cohortRepository;
//    private final ParticipantService participantService;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentRepository studentRepository, ProgramRepository programRepository, CohortRepository cohortRepository
//                             ParticipantService participantService
    ) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
//        this.participantService = participantService;
    }

    @Transactional
    public AttendanceResponse recordAttendance(AttendanceRequest attendanceRequest){

        Student student = studentRepository.findById(attendanceRequest.getStudentId())
                .orElseThrow(() -> new RuntimeException(" Student Not found"));

        if (student.getStatus() == Status.DROPPED_OUT) {
            throw new RuntimeException("Student is dropped out");
        }



        Program program = programRepository.findById(attendanceRequest.getProgramId())
                .orElseThrow(() -> new RuntimeException(" Program Not found"));

        Cohort cohort = cohortRepository.findById(attendanceRequest.getCohortId())
                .orElseThrow(() -> new RuntimeException(" Cohort Not found"));

                if (student.getStatus() == Status.DROPPED_OUT){
            throw new IllegalArgumentException("Student dropout out of the program");
        }
        Attendance attendance = Attendance.builder()

                .attendanceId(UUID.randomUUID())
                .attendanceStatus(AttendanceStatus.valueOf(attendanceRequest
                        .getAttendanceStatus().toUpperCase()))
                .attendanceRecordedDate(attendanceRequest.getAttendanceDate())
                .remarks(attendanceRequest.getRemarks())
                .student(student)
                .program(program)
                .cohort(cohort)
                .recordedById(attendanceRequest.getRecordedById())
                .recordedByName(attendanceRequest.getRecordedByName())
                .build();

        log.info("attendance is saved successfully");
        Attendance saveAttendance = attendanceRepository.save(attendance);

//        participantService.updateProgress(student, student.getProgram());

        return AttendanceMapper.toResponseDTO(saveAttendance);

    }
}
