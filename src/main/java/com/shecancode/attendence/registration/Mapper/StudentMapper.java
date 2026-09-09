package com.shecancode.attendence.registration.Mapper;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;

public class StudentMapper {
    public static StudentResponseDao toDTO (Student student){
        if (student == null)
            return null;

        Program program = student.getProgram();
        Cohort cohort = student.getCohort();
        return  StudentResponseDao.builder()
               .studentId(student.getId())
                .studentFirstName(student.getStudentFirstName())
                .studentLastName(student.getStudentLastName())
                .phoneNumber(student.getPhoneNumber())
                .email(student.getEmail())
                .homeAddress(student.getHomeAddress())
                .programName(program != null ? program.getProgramName() : null)
                .status(student.getStatus())
                .currentOccupation(student.getCurrentOccupation())
                .programStartedDate(cohort != null ? cohort.getStartDate() : null)
                .cohortNumber(cohort != null ? cohort.getCohortNumber() : null)
                .estimateGraduationDate(cohort != null ? cohort.getEndDate() : null)
                .build();
    }

    public static Student toModelStudent(StudentRequestDao requestDao){

        if (requestDao == null) return null;

        Student student = new Student();
        student.setStudentFirstName(requestDao.getStudentFirstName());
        student.setStudentLastName(requestDao.getStudentLastName());
        student.setPhoneNumber(requestDao.getPhoneNumber());
        student.setEmail(requestDao.getEmail());
        student.setHomeAddress(requestDao.getHomeAddress());
        student.setStatus(Status.ACTIVE); // Automatically set new students to ACTIVE
        student.setCohort(Cohort.builder().cohortNumber(requestDao.getCohortNumber()).build());
//        student.set;// Use the saved cohort with its cohortNumber

        student.setCurrentOccupation(requestDao.getCurrentOccupation());
//        student.setDaysRemaining(40);
//        student.setTotalProgramDays(50);
        return student;

    }
    // Overloaded method for backward compatibility
//    public static Student toModelStudent(StudentRequestDao requestDao){
//        return toModelStudent(requestDao, null);
//    }

}
