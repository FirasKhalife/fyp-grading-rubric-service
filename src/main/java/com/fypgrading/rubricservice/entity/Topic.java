package com.fypgrading.rubricservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "assessment_id" }))
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToOne
    @JoinColumn(nullable = false)
    @Enumerated(EnumType.STRING)
    private Assessment assessment;

    @Column(nullable = false)
    private Integer weight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Topic topic)) return false;
        return id.equals(topic.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
