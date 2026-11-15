package com.fypgrading.rubricservice.rubric;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Queries are scoped to the current demo session by Hibernate; the native deletes are for copy management. */
public interface CriterionRepository extends JpaRepository<Criterion, UUID> {

    List<Criterion> findByAssessmentOrderByPosition(Assessment assessment);

    List<Criterion> findAllByOrderByPosition();

    @Modifying
    @Query(value = "delete from criterion where demo_session = :session", nativeQuery = true)
    int deleteCopy(@Param("session") String session);

    @Modifying
    @Query(value = "delete from criterion", nativeQuery = true)
    int deleteAllCopies();
}
