package com.fypgrading.rubricservice.entity;

import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AssessmentEnum name;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Assessment assessment)) return false;
        return id.equals(assessment.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
