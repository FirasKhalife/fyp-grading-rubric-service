package com.fypgrading.rubricservice.service.dto;

import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricDTO {

    private Integer id;

    private String name;

    private Integer percentage;

    private AssessmentEnum assessment;
}
