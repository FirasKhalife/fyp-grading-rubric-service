package com.fypgrading.rubricservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "description", "topic_id" }))
public class Rubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Topic topic;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Level level;

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
