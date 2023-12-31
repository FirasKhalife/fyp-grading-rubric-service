package com.fypgrading.rubricservice.service;

import com.fypgrading.rubricservice.entity.Rubric;
import com.fypgrading.rubricservice.repository.RubricRepository;
import com.fypgrading.rubricservice.service.dto.RubricDTO;
import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import com.fypgrading.rubricservice.service.mapper.RubricMapper;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RubricService {

    private final RubricRepository rubricRepository;
    private final RubricMapper rubricMapper;

    public RubricService(RubricRepository rubricRepository, RubricMapper rubricMapper) {
        this.rubricRepository = rubricRepository;
        this.rubricMapper = rubricMapper;
    }

    @HystrixCommand(fallbackMethod = "handleGetRubricsFallback", commandProperties = {
            @HystrixProperty(name = "execution.isolation.strategy", value = "SEMAPHORE")
    })
    public List<RubricDTO> getRubrics() {
        List<Rubric> rubrics = rubricRepository.findAll();
        return rubricMapper.toDTOList(rubrics);
    }

    @HystrixCommand(fallbackMethod = "handleCreateRubricFallback")
    public RubricDTO createRubric(RubricDTO rubricDTO) {
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        Rubric createdEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(createdEntity);
    }

    @HystrixCommand(fallbackMethod = "handleUpdateRubricFallback")

    public RubricDTO updateRubric(Integer id, RubricDTO rubricDTO) {
        getRubricById(id);
        Rubric rubric = rubricMapper.toEntity(rubricDTO);
        rubric.setId(id);
        Rubric updatedEntity = rubricRepository.save(rubric);
        return rubricMapper.toDTO(updatedEntity);
    }

    @HystrixCommand(fallbackMethod = "handleDeleteRubricFallback")

    public RubricDTO deleteRubric(Integer id) {
        Rubric rubric = getRubricById(id);
        rubricRepository.delete(rubric);
        return rubricMapper.toDTO(rubric);
    }

    private Rubric getRubricById(Integer id) {
        return rubricRepository.findById(id).orElseThrow(() ->
                        new EntityNotFoundException("Rubric not found"));
    }

    public List<RubricDTO> getRubricsByAssessment(String assessmentStr) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Rubric> rubrics = rubricRepository.findByAssessment(assessment);
        return rubricMapper.toDTOList(rubrics);
    }

    @Transactional
    public List<RubricDTO> updateRubricsByAssessment(String assessmentStr, List<RubricDTO> rubricDTOList) {
        AssessmentEnum assessment = AssessmentEnum.valueOf(assessmentStr.toUpperCase());
        List<Rubric> rubrics = rubricMapper.toEntityList(rubricDTOList);
        rubricRepository.deleteAllByAssessment(assessment);
        List<Rubric> updatedEntities = rubricRepository.saveAll(rubrics);
        return rubricMapper.toDTOList(updatedEntities);
    }
}
