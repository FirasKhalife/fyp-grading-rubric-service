package com.fypgrading.rubricservice.repository;

import com.fypgrading.rubricservice.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {
//    List<Rubric> findByAssessment(AssessmentEnum assessment);
//
//    void deleteAllByAssessment(AssessmentEnum assessment);
}
