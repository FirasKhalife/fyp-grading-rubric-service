package com.fypgrading.rubricservice.service.mapper;

import com.fypgrading.rubricservice.entity.Level;
import com.fypgrading.rubricservice.service.dto.LevelDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LevelMapper extends EntityMapper<Level, LevelDTO> {
}
