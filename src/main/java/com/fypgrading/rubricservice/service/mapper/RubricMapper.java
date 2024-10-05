package com.fypgrading.rubricservice.service.mapper;

import com.fypgrading.rubricservice.entity.Rubric;
import com.fypgrading.rubricservice.service.dto.RubricDTO;
import org.mapstruct.Mapper;

@Mapper(
    componentModel = "spring",
    uses = {
        TopicMapper.class,
        LevelMapper.class,
    }
)
public interface RubricMapper extends EntityMapper<Rubric, RubricDTO> {
}
