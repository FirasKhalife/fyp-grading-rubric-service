package com.fypgrading.rubricservice.repository;

import com.fypgrading.rubricservice.entity.Rubric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {

    List<Rubric> findByTopicAssessment_Name(String assessmentName);

    void deleteAllByTopicAssessment_Name(String assessmentName);

}
