package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.Attendence.Repo.AttendanceRepository;
import com.shecancode.attendence.auth.model.AppUser;
import com.shecancode.attendence.auth.repository.UserRepository;
import com.shecancode.attendence.registration.Enum.LifecycleStatus;
import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Exception.InvalidStatusTransitionException;
import com.shecancode.attendence.registration.Exception.ReadOnlyException;
import com.shecancode.attendence.registration.Exception.ResourceInUseException;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.CohortRepository;
import com.shecancode.attendence.registration.Repository.ProgramRepository;
import com.shecancode.attendence.registration.Repository.StudentProfileRepository;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.CohortProgressResponse;
import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Closed = read-only, soft delete guards, and the student status lifecycle. */
@ExtendWith(MockitoExtension.class)
class LifecycleRulesTest {

    @Mock private ProgramRepository programRepository;
    @Mock private CohortRepository cohortRepository;
    @Mock private StudentRepository studentRepository;
    @Mock private UserRepository userRepository;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private StudentProfileRepository profileRepository;
    @Mock private StudentProfileService profileService;

    private ProgramService programService;
    private CohortService cohortService;
    private StudentLifeCycleService studentService;

    private Program program;
    private Cohort cohort;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-03-10T08:00:00Z"), ZoneOffset.UTC);
        programService = new ProgramService(programRepository, cohortRepository, studentRepository);
        cohortService = new CohortService(cohortRepository, programRepository, studentRepository, clock);
        studentService = new StudentLifeCycleService(studentRepository, userRepository, attendanceRepository,
                profileRepository, profileService);

        program = Program.builder().id(UUID.randomUUID()).programName("Backend").build();
        cohort = Cohort.builder().id(UUID.randomUUID()).cohortNumber("C1").program(program)
                .startDate(LocalDate.of(2026, 3, 1)).endDate(LocalDate.of(2026, 5, 31)).build();
        lenient().when(programRepository.findByIdAndDeletedAtIsNull(program.getId())).thenReturn(Optional.of(program));
        lenient().when(cohortRepository.findByIdAndDeletedAtIsNull(cohort.getId())).thenReturn(Optional.of(cohort));
        lenient().when(programRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(cohortRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(studentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Student student(Status status) {
        Student s = Student.builder().id(UUID.randomUUID()).email("s@x.org").status(status)
                .cohort(cohort).program(program).user(AppUser.builder().enabled(true).build()).build();
        lenient().when(studentRepository.findByIdAndDeletedAtIsNull(s.getId())).thenReturn(Optional.of(s));
        return s;
    }

    @Nested
    class ClosedIsReadOnly {

        @Test
        void closedProgramMakesItsCohortsReadOnly() {
            program.setStatus(LifecycleStatus.CLOSED);
            assertTrue(cohort.isReadOnly());
        }

        @Test
        void closedProgramCannotBeUpdated() {
            program.setStatus(LifecycleStatus.CLOSED);
            ProgramRequestDao request = ProgramRequestDao.builder().programName("New name").build();

            assertThrows(ReadOnlyException.class, () -> programService.updateProgram(program.getId(), request));
        }

        @Test
        void closedProgramCannotGetNewCohorts() {
            program.setStatus(LifecycleStatus.CLOSED);
            CohortRequestDao request = CohortRequestDao.builder().cohortNumber("C2").programId(program.getId()).build();

            assertThrows(ReadOnlyException.class, () -> cohortService.createCohort(request));
        }

        @Test
        void closedCohortCannotBeUpdated() {
            cohort.setStatus(LifecycleStatus.CLOSED);
            CohortRequestDao request = CohortRequestDao.builder().cohortNumber("C1b").programId(program.getId()).build();

            assertThrows(ReadOnlyException.class, () -> cohortService.updateCohort(cohort.getId(), request));
        }

        @Test
        void closedCohortCanBeReopened() {
            cohort.setStatus(LifecycleStatus.CLOSED);

            assertEquals(LifecycleStatus.OPEN, cohortService.changeStatus(cohort.getId(), LifecycleStatus.OPEN).getStatus());
        }

        @Test
        void cohortCannotBeReopenedWhileProgramClosed() {
            program.setStatus(LifecycleStatus.CLOSED);
            cohort.setStatus(LifecycleStatus.CLOSED);

            assertThrows(ReadOnlyException.class, () -> cohortService.changeStatus(cohort.getId(), LifecycleStatus.OPEN));
        }

        @Test
        void studentsOfClosedCohortCannotChange() {
            cohort.setStatus(LifecycleStatus.CLOSED);
            Student s = student(Status.ACTIVE);

            assertThrows(ReadOnlyException.class, () -> studentService.changeStatus(s.getId(), Status.GRADUATED));
            assertThrows(ReadOnlyException.class, () -> studentService.deleteStudent(s.getId()));
        }
    }

    @Nested
    class SoftDelete {

        @Test
        void programWithCohortsCannotBeDeleted() {
            when(cohortRepository.countByProgram_IdAndDeletedAtIsNull(program.getId())).thenReturn(2L);

            assertThrows(ResourceInUseException.class, () -> programService.deleteProgram(program.getId()));
            assertNull(program.getDeletedAt());
        }

        @Test
        void emptyProgramIsSoftDeleted() {
            when(cohortRepository.countByProgram_IdAndDeletedAtIsNull(program.getId())).thenReturn(0L);

            programService.deleteProgram(program.getId());

            assertNotNull(program.getDeletedAt());
            verify(programRepository, never()).delete(any(Program.class));
        }

        @Test
        void cohortWithStudentsCannotBeDeleted() {
            when(studentRepository.countByCohort_IdAndDeletedAtIsNull(cohort.getId())).thenReturn(5L);

            assertThrows(ResourceInUseException.class, () -> cohortService.deleteCohort(cohort.getId()));
        }

        @Test
        void deletedStudentIsHiddenAndCannotLogIn() {
            Student s = student(Status.ACTIVE);

            studentService.deleteStudent(s.getId());

            assertNotNull(s.getDeletedAt());
            assertFalse(s.getUser().isEnabled());
            verify(studentRepository, never()).delete(any(Student.class));
        }
    }

    @Nested
    class StudentStatus {

        @ParameterizedTest
        @CsvSource({"ACTIVE,INACTIVE", "ACTIVE,GRADUATED", "ACTIVE,DROPPED_OUT", "INACTIVE,ACTIVE",
                "INACTIVE,DROPPED_OUT", "PENDING,DROPPED_OUT"})
        void allowedChanges(Status from, Status to) {
            Student s = student(from);

            assertEquals(to, studentService.changeStatus(s.getId(), to).getStatus());
        }

        @ParameterizedTest
        @CsvSource({"PENDING,ACTIVE", "GRADUATED,ACTIVE", "DROPPED_OUT,ACTIVE", "ACTIVE,ACTIVE",
                "INACTIVE,GRADUATED", "ACTIVE,PENDING"})
        void refusedChanges(Status from, Status to) {
            Student s = student(from);

            assertThrows(InvalidStatusTransitionException.class, () -> studentService.changeStatus(s.getId(), to));
            assertEquals(from, s.getStatus());
        }
    }

    @Nested
    class Progress {

        private final LocalDate start = LocalDate.of(2026, 3, 2);
        private final LocalDate end = LocalDate.of(2026, 3, 29); // 28 days = 4 weeks

        @Test
        void beforeStart() {
            CohortProgressResponse p = CohortService.progress(null, start, end, start.minusDays(3));
            assertEquals(0, p.currentWeek());
            assertEquals(0, p.daysElapsed());
            assertEquals(0.0, p.percentComplete());
        }

        @Test
        void firstDayIsWeekOne() {
            CohortProgressResponse p = CohortService.progress(null, start, end, start);
            assertEquals(1, p.currentWeek());
            assertEquals(4, p.totalWeeks());
            assertEquals(1, p.daysElapsed());
            assertEquals(27, p.daysRemaining());
        }

        @Test
        void eighthDayIsWeekTwo() {
            CohortProgressResponse p = CohortService.progress(null, start, end, start.plusDays(7));
            assertEquals(2, p.currentWeek());
            assertEquals(28.6, p.percentComplete());
        }

        @Test
        void afterEnd() {
            CohortProgressResponse p = CohortService.progress(null, start, end, end.plusDays(10));
            assertEquals(4, p.currentWeek());
            assertEquals(28, p.daysElapsed());
            assertEquals(0, p.daysRemaining());
            assertEquals(100.0, p.percentComplete());
        }
    }
}
