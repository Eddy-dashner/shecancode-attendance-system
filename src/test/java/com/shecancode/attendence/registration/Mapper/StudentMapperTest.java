package com.shecancode.attendence.registration.Mapper;

import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;
import com.shecancode.attendence.registration.dao.StudentRequestDao;
import com.shecancode.attendence.registration.dao.StudentResponseDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StudentMapperTest {

    @Mock
    Student student;


    @Test
    void testMappingStudent_toResponseDTO_isSuccessful() {
        //arranging

        Student student = Student.builder()
                .id(UUID.randomUUID())
                .studentFirstName("bob")
                .studentLastName("BOZ")
                .email("innocent@gmail.com")
                .phoneNumber("12 strt")
                .homeAddress("student")
                .currentOccupation("Employeed")
                .status(Status.ACTIVE)
                .program(Program.builder().programName("BACKEND").build())
                .cohort(Cohort.builder().cohortNumber("cohort-10").build())
//                .totalGraduationDays(200)
                .build();
         //act: map entity to dto
        StudentResponseDao studentResponseDao = StudentMapper.toDTO(student);
        //assertions:
        assertNotNull(studentResponseDao);
        assertEquals(student.getStudentFirstName(),(studentResponseDao.getStudentFirstName()));
    }
    @Test
    void toModelStudent() {
        StudentRequestDao requestDao =  StudentRequestDao.builder()
                .studentFirstName("bob")
                .studentLastName("BOZ")
                .email("innocent@gmail.com")
                .phoneNumber("12 strt")
                .homeAddress("student")
                .currentOccupation("Employeed")
                .programName("BACKEND")
                .cohortNumber("cohort-10")
                .build();

        Student studentModel = StudentMapper.toModelStudent(requestDao);

        assertNotNull(studentModel);
        assertEquals("innocent@gmail.com", studentModel.getEmail());


    }

}