package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j

public class StudentLifeCycleService {

    private final StudentRepository repository;

    public StudentLifeCycleService(StudentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markDropout(UUID studentId) {
        Student student = repository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (student.getStatus() == Status.DROPPED_OUT) {
            throw new IllegalArgumentException("Student is already DROPPED_OUT");
        }

        student.setStatus(Status.DROPPED_OUT);
        repository.save(student);

        log.info("Student {} marked as DROPPED_OUT", student.getEmail());
    }

}




