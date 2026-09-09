package com.shecancode.attendence.registration.Mapper;

import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.Model.Cohort;
import com.shecancode.attendence.registration.Model.Program;

public class CohortMapper {

    public static CohortResponseDao toCohortResponseDao(Cohort cohort) {
        if (cohort == null)
            return null;

        Program program = cohort.getProgram();
        return CohortResponseDao.builder()
                .cohortNumber(cohort.getCohortNumber())
                .startDate(cohort.getStartDate())
                .endDate(cohort.getEndDate())
                .programId(program != null ? program.getId() : null)
                .programName(program != null ? program.getProgramName() : null)
                .build();
    }


    public static Cohort toEntityCohort(CohortRequestDao requestDao) {

        return Cohort.builder()
                .cohortNumber(requestDao.getCohortNumber())
                .startDate(requestDao.getStartDate())
                .endDate(requestDao.getEndDate())
                .build();
    }
}
