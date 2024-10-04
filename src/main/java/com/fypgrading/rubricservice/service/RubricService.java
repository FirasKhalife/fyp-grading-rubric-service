package com.fypgrading.rubricservice.service;

import com.fypgrading.rubricservice.entity.Rubric;
import com.fypgrading.rubricservice.repository.RubricRepository;
import com.fypgrading.rubricservice.service.dto.RubricDTO;
import com.fypgrading.rubricservice.service.mapper.RubricMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class RubricService {

    private final RubricRepository rubricRepository;
    private final RubricMapper rubricMapper;

    public List<RubricDTO> getRubrics() {
        List<Rubric> rubrics = rubricRepository.findAll();
        return rubricMapper.toDTOList(rubrics);
    }

    public RubricDTO createRubric(RubricDTO rubricDTO) {
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        Rubric createdEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(createdEntity);
    }

    public RubricDTO updateRubric(Long id, RubricDTO rubricDTO) {
        getRubricById(id);
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        rubric.setId(id);
        Rubric updatedEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(updatedEntity);
    }

    public RubricDTO deleteRubric(Long id) {
        Rubric rubric = getRubricById(id);
        rubricRepository.delete(rubric);
        return rubricMapper.toDTO(rubric);
    }

    private Rubric getRubricById(Long id) {
        return rubricRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Rubric not found"));
    }

    public List<RubricDTO> getRubricsByAssessment(String assessmentStr) {
        List<Rubric> rubrics = rubricRepository.findByTopicAssessment_Name(assessmentStr.toUpperCase());
        return rubricMapper.toDTOList(rubrics);
    }

    @Transactional
    public List<RubricDTO> updateRubricsByAssessment(String assessmentStr, List<RubricDTO> rubricDTOList) {
        List<Rubric> rubrics = rubricMapper.toEntityList(rubricDTOList);
        rubricRepository.deleteAllByTopicAssessment_Name(assessmentStr.toUpperCase());
        List<Rubric> updatedEntities = rubricRepository.saveAll(rubrics);
        return rubricMapper.toDTOList(updatedEntities);
    }
}
