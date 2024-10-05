package com.fypgrading.rubricservice.service.dto;

import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TopicDTO {

    @NotNull
    private String name;

    @NotNull
    private Integer number;

    @NotNull
    private AssessmentEnum assessment;

    @NotNull
    private Integer weight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TopicDTO topicDTO)) return false;
        return name.equals(topicDTO.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
