package com.fypgrading.rubricservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RubricDTOList {

    private List<RubricDTO> rubrics;

}
