package com.fypgrading.rubricservice.repository;

import com.fypgrading.rubricservice.entity.Rubric;
import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {
    List<Rubric> findByAssessment(AssessmentEnum assessment);

    void deleteAllByAssessment(AssessmentEnum assessment);
}
