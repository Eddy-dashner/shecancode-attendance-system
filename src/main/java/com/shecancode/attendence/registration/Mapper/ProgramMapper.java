package com.shecancode.attendence.registration.Mapper;

import com.shecancode.attendence.registration.Model.Program;
import com.shecancode.attendence.registration.dao.ProgramRequestDao;
import com.shecancode.attendence.registration.dao.ProgramResponseDao;

public class ProgramMapper {
    public static Program ToProgramEntity(ProgramRequestDao programRequestDao){

        return Program.builder()
                .programName(programRequestDao.getProgramName())
                .programDuration(programRequestDao.getProgramDuration())
                .programStartDate(programRequestDao.getProgramStartDate())
                .programEndDate(programRequestDao.getProgramEndDate())
                .build();
    }

    public static ProgramResponseDao ToResponseDao(Program program){
        return ProgramResponseDao.builder()
                .cohort(program.getCohort())
                .programDuration(program.getProgramDuration())
                .programName(program.getProgramName())
                .programStartDate(program.getProgramStartDate())
                .programEndDate(program.getProgramEndDate())

                .build();
    }
}
