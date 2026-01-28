package com.shecancode.attendence.registration.service;

import com.shecancode.attendence.registration.Exception.EmailAlreadyExistException;
import com.shecancode.attendence.registration.Mapper.StudentMapper;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.Repository.StudentRepository;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import org.springframework.stereotype.Service;

@Service
public class StudentRegistrationService {

    private final StudentRepository studentRepository;

    public StudentRegistrationService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public StudentResponseDao createStudent(StudentRequestDao studentRequestDao){
        if ( studentRepository.existByEmail(studentRequestDao.getEmail()).isPresent()){
            throw new EmailAlreadyExistException(" A patient with this email exist");
        }

        Student newStudent = studentRepository.save(StudentMapper.toModelStudent(studentRequestDao));
        return StudentMapper.toDTO(newStudent);

    }
}
