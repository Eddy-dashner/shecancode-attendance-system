package com.shecancode.attendence.registration.Mapper;

import com.shecancode.attendence.registration.dao.CohortRequestDao;
import com.shecancode.attendence.registration.dao.CohortResponseDao;
import com.shecancode.attendence.registration.Model.Cohort;

public class CohortMapper {

    public static CohortResponseDao toCohortResponseDao(Cohort cohort) {
        if (cohort == null)
            return null;

        return CohortResponseDao.builder()
                .cohortNumber(cohort.getCohortNumber())
                .startDate(cohort.getStartDate())
                .endDate(cohort.getEndDate())
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
