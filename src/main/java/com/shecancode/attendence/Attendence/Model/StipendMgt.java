package com.shecancode.attendence.Attendence.Model;


import com.shecancode.attendence.registration.Enum.Status;
import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.Model.Student;

import java.util.Date;

public class StipendMgt {
    private String stipendId;

    private Student student;

    private Program program;

    private double amount;

    private Status status;

    private Date startDate;

    private Date endDate;
}
