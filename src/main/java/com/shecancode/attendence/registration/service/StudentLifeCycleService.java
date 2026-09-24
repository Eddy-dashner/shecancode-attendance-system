package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.Attendence.Model.Attendance;
import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.Attendence.Service.AttendanceScore;
import com.shecancode.attendence.Attendence.dao.AttendanceSummaryDto;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.InvalidStatusTransitionException;
import com.shecancode.attendence.registration.Exception.ReadOnlyException;
import com.shecancode.attendence.registration.Exception.StudentDroppedOutException;
import com.shecancode.attendence.registration.Exception.StudentNotFoundException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.Repository.StudentSpecs;
import com.shecancode.attendence.registration.dao.CompleteProfileRequest;
import com.shecancode.attendence.registration.dao.PageResponse;
import com.shecancode.attendence.registration.dao.StudentDetailResponse;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Admin-side student management. Status lifecycle:
 * <pre>
 *   PENDING  -> DROPPED_OUT            (ACTIVE is reached by the student activating)
 *   ACTIVE   -> INACTIVE | DROPPED_OUT | GRADUATED
 *   INACTIVE -> ACTIVE | DROPPED_OUT
 *   GRADUATED, DROPPED_OUT are final
 * </pre>
 * No change is allowed while the student's cohort (or its program) is closed.
 */
@Service
@Slf4j
public class StudentLifeCycleService {

    public static final int MAX_PAGE_SIZE = 100;

    private static final Map<Status, Set<Status>> ALLOWED = Map.of(
            Status.PENDING, Set.of(Status.DROPPED_OUT),
            Status.ACTIVE, Set.of(Status.INACTIVE, Status.DROPPED_OUT, Status.GRADUATED),
            Status.INACTIVE, Set.of(Status.ACTIVE, Status.DROPPED_OUT),
            Status.GRADUATED, Set.of(),
            Status.DROPPED_OUT, Set.of());

    private final StudentRepository repository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;

    public StudentLifeCycleService(StudentRepository repository, UserRepository userRepository,
                                   AttendanceRepository attendanceRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
    }

    /** All filters are optional; results are ordered by name. */
    @Transactional(readOnly = true)
    public PageResponse<StudentResponseDao> search(UUID programId, UUID cohortId, Status status, String search,
                                                   int page, int size) {
        if (page < 0) throw new IllegalArgumentException("page must be 0 or more");
        if (size < 1 || size > MAX_PAGE_SIZE) throw new IllegalArgumentException("size must be between 1 and " + MAX_PAGE_SIZE);
        return PageResponse.of(
                repository.findAll(StudentSpecs.matching(programId, cohortId, status, search),
                        PageRequest.of(page, size, Sort.by("studentFirstName", "studentLastName", "email"))),
                StudentMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public StudentDetailResponse getStudent(UUID studentId) {
        Student student = findStudent(studentId);
        List<Attendance> history = student.getProgram() == null ? List.of()
                : attendanceRepository.findHistory(studentId, student.getProgram().getId());
        AttendanceScore score = AttendanceScore.of(history.stream().map(Attendance::getAttendanceStatus).toList());
        return new StudentDetailResponse(StudentMapper.toDTO(student), AttendanceSummaryDto.of(score));
    }

    @Transactional
    public StudentResponseDao changeStatus(UUID studentId, Status newStatus) {
        Student student = findStudent(studentId);
        requireWritable(student);
        if (!ALLOWED.get(student.getStatus()).contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    "Cannot change a student from " + student.getStatus() + " to " + newStatus);
        }
        student.setStatus(newStatus);
        log.info("Student {} is now {}", studentId, newStatus);
        return StudentMapper.toDTO(repository.save(student));
    }

    @Transactional
    public void markDropout(UUID studentId) {
        Student student = findStudent(studentId);
        if (student.getStatus() == Status.DROPPED_OUT) {
            throw new StudentDroppedOutException("Student is already DROPPED_OUT");
        }
        changeStatus(studentId, Status.DROPPED_OUT);
    }

    /** Admin edit of the profile fields. Email, program and cohort are not changed here. */
    @Transactional
    public StudentResponseDao updateStudent(UUID studentId, CompleteProfileRequest request) {
        Student student = findStudent(studentId);
        requireWritable(student);
        student.setStudentFirstName(request.getStudentFirstName());
        student.setStudentLastName(request.getStudentLastName());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setHomeAddress(request.getHomeAddress());
        student.setCurrentOccupation(request.getCurrentOccupation());

        AppUser user = student.getUser();
        if (user != null) {
            user.setFullName(student.getFullName());
            userRepository.save(user);
        }
        log.info("Student {} updated by admin", studentId);
        return StudentMapper.toDTO(repository.save(student));
    }

    /** Soft delete: hidden everywhere, attendance history kept, login disabled. */
    @Transactional
    public void deleteStudent(UUID studentId) {
        Student student = findStudent(studentId);
        requireWritable(student);
        student.setDeletedAt(Instant.now());
        AppUser user = student.getUser();
        if (user != null) {
            user.setEnabled(false);
            userRepository.save(user);
        }
        repository.save(student);
        log.info("Student {} deleted", studentId);
    }

    private Student findStudent(UUID studentId) {
        return repository.findByIdAndDeletedAtIsNull(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
    }

    private static void requireWritable(Student student) {
        if (student.getCohort() != null && student.getCohort().isReadOnly()) {
            throw new ReadOnlyException("Cohort " + student.getCohort().getCohortNumber()
                    + " is closed; reopen it to change its students.");
        }
    }
}
