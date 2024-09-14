package com.fypgrading.rubricservice.entity;

import com.fypgrading.rubricservice.service.enums.AssessmentEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private Integer percentage;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AssessmentEnum assessment;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Rubric rubric)) return false;
        return id.equals(rubric.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
