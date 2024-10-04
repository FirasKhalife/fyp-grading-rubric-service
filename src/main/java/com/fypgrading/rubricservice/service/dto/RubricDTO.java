package com.fypgrading.rubricservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricDTO {

    private String description;

    private TopicDTO topic;

    private LevelDTO level;

}
