package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.dao.AttendanceResponse;
import com.shecancode.attendence.Attendence.dao.BulkAttendanceRequest;
import com.shecancode.attendence.Attendence.dao.StudentAttendanceRequestDto;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.ResourceNotFoundException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ProgramRepository programRepository;
    private final CohortRepository cohortRepository;
    private final ParticipantService participantService;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository,
                             ProgramRepository programRepository, CohortRepository cohortRepository, ParticipantService participantService) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
        this.participantService = participantService;
    }

    @Transactional
    public List<AttendanceResponse> recordBulkAttendance(BulkAttendanceRequest request, UUID programId, UUID cohortId) {

        // 1️⃣ Fetch Program & Cohort
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found"));

        // 2️⃣ Pre-fetch students
        List<UUID> studentIds = request.getStudents().stream()
                .map(StudentAttendanceRequestDto::getStudentId)
                .toList();

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        // 3️⃣ Check for duplicates
        Set<UUID> existingAttendanceIds = attendanceRepository
                .findStudentIdsByDateAndCohort(request.getAttendanceDate(), cohortId);

        List<Attendance> attendancesToSave = new ArrayList<>();

        // 4️⃣ Process the list
        for (StudentAttendanceRequestDto studentDto : request.getStudents()) {
            Student student = studentMap.get(studentDto.getStudentId());

            if (student == null || student.getStatus() == Status.DROPPED_OUT) continue;

            if (!student.getProgram().getId().equals(programId) || !student.getCohort().getId().equals(cohortId)) continue;

            if (existingAttendanceIds.contains(student.getId())) continue;

            Attendance attendance = AttendanceMapper.toAttendance(
                    studentDto, student, program, cohort,
                    request.getRecordedById(), request.getRecordedByName(), request.getAttendanceDate()
            );

            attendancesToSave.add(attendance);
        }

        if (attendancesToSave.isEmpty()) return Collections.emptyList();

        // 7️⃣ Bulk Save
        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendancesToSave);


        savedAttendances.stream()
                .map(Attendance::getStudent)
                .distinct()
                .forEach(student -> participantService.updateProgress(student, program));

        // Calculate "Trucker" Countdown
        Integer daysRemaining = calculateRemainingDays(programId, program.getProgramDuration());

        return savedAttendances.stream()
                .map(attendance -> AttendanceMapper.toResponseDTO(attendance, daysRemaining))
                .toList();
    }

    @Transactional
    public List<AttendanceResponse> updateBulkAttendance(BulkAttendanceRequest request, UUID programId, UUID cohortId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new RuntimeException("Program not found with id: " + programId));

        List<UUID> studentIds = request.getStudents().stream()
                .map(StudentAttendanceRequestDto::getStudentId).toList();

        List<Attendance> existingRecords = attendanceRepository
                .findByAttendanceRecordedDateAndCohortIdAndStudentIdIn(
                        request.getAttendanceDate(),
                        cohortId,
                        studentIds
                );
        Map<UUID, Attendance> attendanceMap = existingRecords.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        List<Attendance> toUpdate = new ArrayList<>();

        for (StudentAttendanceRequestDto dto : request.getStudents()) {
            Attendance attendance = attendanceMap.get(dto.getStudentId());
            if (attendance != null) {
                attendance.setAttendanceStatus(dto.getAttendanceStatus());
                attendance.setCheckInTime(dto.getCheckInTime());
                attendance.setRemarks(dto.getRemarks());
                attendance.setRecordedByName(request.getRecordedByName());
                toUpdate.add(attendance);
            }
        }

        List<Attendance> saved = attendanceRepository.saveAll(toUpdate);

        saved.stream()
                .map(Attendance::getStudent)
                .distinct()
                .forEach(student -> participantService.updateProgress(student, program));

        Integer daysRemaining = calculateRemainingDays(programId, program.getProgramDuration());

        return saved.stream()
                .map(attendance -> AttendanceMapper.toResponseDTO(attendance, daysRemaining))
                .toList();
    }

    public Integer calculateRemainingDays(UUID programId, int totalDuration) {
        long daysConducted = attendanceRepository.countDistinctDatesByProgramId(programId);
        int remaining = totalDuration - (int) daysConducted;
        return Math.max(0, remaining);
    }

}
