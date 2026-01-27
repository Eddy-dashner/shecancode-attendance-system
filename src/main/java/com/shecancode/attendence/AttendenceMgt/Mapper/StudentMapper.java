package com.shecancode.attendence.AttendenceMgt.Mapper;

import com.shecancode.attendence.AttendenceMgt.Model.Student;
import com.shecancode.attendence.AttendenceMgt.dao.StudentRequestDao;
import com.shecancode.attendence.AttendenceMgt.dao.StudentResponseDao;

import java.util.UUID;

public class StudentMapper {

    public static StudentResponseDao toDTO (Student student){
        if (student == null)
            return null;

        return  StudentResponseDao.builder()
               .studentId(student.getStudentId())
                .student_FirstName(student.getStudent_FirstName())
                .student_Lastname(student.getStudent_Lastname())
                .email(student.getEmail())
                .home_address(student.getHome_address())
                .status(student.getStatus())
                .cohort_Id(student.getCohort_Id())
                .current_occupation(student.getCurrent_occupation())
                .registration_period(student.getRegistration_period())
                .build();
    }

    public static Student toModelStudent(StudentRequestDao requestDao){

        if (requestDao == null) return null;

        Student student = new Student();
        student.setStudent_FirstName(requestDao.getStudent_FirstName());
        student.setStudent_Lastname(requestDao.getStudent_Lastname());
        student.setPhoneNumber(requestDao.getPhoneNumber());
        student.setEmail(requestDao.getEmail());
        student.setHome_address(requestDao.getHome_address());
        student.setCohort_Id(requestDao.getCohort_Id());
        student.setStatus(requestDao.getStatus());
        student.setCurrent_occupation(requestDao.getCurrent_occupation());

        if (requestDao.getRegistration_period() != null){
            student.setRegistration_period(requestDao.getRegistration_period());
        }
        return student;

    }

}
