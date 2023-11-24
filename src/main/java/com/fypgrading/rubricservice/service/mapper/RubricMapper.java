package com.fypgrading.rubricservice.service.mapper;

import com.fypgrading.rubricservice.entity.Rubric;
import com.fypgrading.rubricservice.service.dto.RubricDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RubricMapper {

    RubricDTO toDTO(Rubric rubric);

    Rubric toEntity(RubricDTO rubricDTO);

    List<RubricDTO> toDTOList(List<Rubric> rubrics);

    List<Rubric> toEntityList(List<RubricDTO> rubricDTOs);
}
