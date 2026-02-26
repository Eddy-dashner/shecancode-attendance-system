package com.shecancode.attendence.Attendence.Service;

import com.shecancode.attendence.Attendence.Mapper.AttendanceMapper;
import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.dto.AttendanceResponse;
import com.shecancode.attendence.Attendence.dto.BulkAttendanceRequest;
import com.shecancode.attendence.Attendence.dto.StudentAttendanceRequestDto;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.ResourceNotFoundException;
import com.shecancode.attendence.registration.Exception.StudentNotFoundException;
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

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository, ProgramRepository programRepository, CohortRepository cohortRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.programRepository = programRepository;
        this.cohortRepository = cohortRepository;
    }


    // private final ParticipantService participantService; // optional

    @Transactional
    public List<AttendanceResponse> recordBulkAttendance(BulkAttendanceRequest request, UUID programId, UUID cohortId) {

        // 1️⃣ Fetch Program & Cohort (Single Fetch)
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

        Cohort cohort = cohortRepository.findById(cohortId)
                .orElseThrow(() -> new ResourceNotFoundException("Cohort not found"));

        // 2️⃣ Pre-fetch all students in the request to avoid querying inside the loop
        List<UUID> studentIds = request.getStudents().stream()
                .map(StudentAttendanceRequestDto::getStudentId)
                .toList();

        Map<UUID, Student> studentMap = studentRepository.findAllById(studentIds).stream()
                .collect(Collectors.toMap(Student::getId, s -> s));

        // 3️⃣ Pre-fetch existing attendance for this date/cohort to prevent duplicates efficiently
        Set<UUID> existingAttendanceIds = attendanceRepository
                .findStudentIdsByDateAndCohort(request.getAttendanceDate(), cohortId);

        List<Attendance> attendancesToSave = new ArrayList<>();

        // 4️⃣ Process the list
        for (StudentAttendanceRequestDto studentDto : request.getStudents()) {
            Student student = studentMap.get(studentDto.getStudentId());

            if (student == null) {
                log.warn("Student ID {} not found in database", studentDto.getStudentId());
                continue;
            }

            // 5️⃣ Validation Logic
            if (student.getStatus() == Status.DROPPED_OUT) {
                log.warn("Skipping dropped-out student: {}", student.getEmail());
                continue;
            }

            if (!student.getProgram().getId().equals(programId) || !student.getCohort().getId().equals(cohortId)) {
                log.warn("Skipping student {}: mismatch in program/cohort", student.getEmail());
                continue;
            }

            if (existingAttendanceIds.contains(student.getId())) {
                log.warn("Attendance already exists for student {} on {}", student.getEmail(), request.getAttendanceDate());
                continue;
            }

            // 6️⃣ Map to entity (No DB hits here)
            Attendance attendance = AttendanceMapper.toAttendance(
                    studentDto, student, program, cohort,
                    request.getRecordedById(), request.getRecordedByName(), request.getAttendanceDate()
            );

            attendancesToSave.add(attendance);
        }

        // 7️⃣ Bulk Save (Significant performance boost)
        if (attendancesToSave.isEmpty()) {
            log.warn("No new attendance records to save for cohort {}", cohort.getCohortNumber());
            return Collections.emptyList();
        }

        List<Attendance> savedAttendances = attendanceRepository.saveAll(attendancesToSave);

        log.info("Successfully recorded attendance for {} students", savedAttendances.size());

        // 8️⃣ Map back to Response DTOs
        return savedAttendances.stream()
                .map(AttendanceMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public List<AttendanceResponse> updateBulkAttendance(BulkAttendanceRequest request, UUID programId, UUID cohortId) {
        // 1. Fetch records that ALREADY exist
        List<UUID> studentIds = request.getStudents().stream()
                .map(StudentAttendanceRequestDto::getStudentId).toList();

        // Find existing entities to update
        List<Attendance> existingRecords = attendanceRepository
        .findByAttendanceRecordedDateAndCohortIdAndStudentIdIn(request.getAttendanceDate(), cohortId, studentIds );

        // Create a map for quick lookup
        Map<UUID, Attendance> attendanceMap = existingRecords.stream()
                .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        List<Attendance> toUpdate = new ArrayList<>();

        for (StudentAttendanceRequestDto dto : request.getStudents()) {
            Attendance attendance = attendanceMap.get(dto.getStudentId());

            if (attendance != null) {
                // Update fields
                attendance.setAttendanceStatus(dto.getAttendanceStatus());
                attendance.setCheckInTime(dto.getCheckInTime());
                attendance.setRemarks(dto.getRemarks());
                attendance.setRecordedByName(request.getRecordedByName());
                toUpdate.add(attendance);
            }
        }

        return attendanceRepository.saveAll(toUpdate).stream()
                .map(AttendanceMapper::toResponseDTO)
                .toList();
    }

}
