package com.fypgrading.rubricservice.service.mapper;

import com.fypgrading.rubricservice.entity.Assessment;
import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    default Assessment toAssessment(AssessmentEnum assessmentEnum) {
        return new Assessment(assessmentEnum);
    }

    default AssessmentEnum toEnum(Assessment assessment) {
        return assessment.getName();
    }

}
