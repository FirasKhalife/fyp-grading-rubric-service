package com.fypgrading.rubricservice.service.mapper;

import com.fypgrading.rubricservice.entity.Topic;
import com.fypgrading.rubricservice.service.dto.TopicDTO;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = { AssessmentMapper.class }
)
public interface TopicMapper extends EntityMapper<Topic, TopicDTO> {
}
