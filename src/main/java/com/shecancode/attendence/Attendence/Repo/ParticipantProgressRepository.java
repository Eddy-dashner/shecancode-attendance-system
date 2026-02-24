//package com.shecancode.attendence.Attendence.Repo;
//
//import com.shecancode.attendence.Attendence.Model.ParticipantProgress;
//import com.shecancode.attendence.registration.Model.Program;
//import com.shecancode.attendence.registration.Model.Student;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//public interface ParticipantProgressRepository extends JpaRepository<ParticipantProgress, UUID> {
//
//    Optional<ParticipantProgress> findByStudentAndProgram(Student student, Program program);
//}
