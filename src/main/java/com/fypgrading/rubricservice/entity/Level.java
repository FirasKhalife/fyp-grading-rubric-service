package com.fypgrading.rubricservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Level {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private Integer weight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Level level)) return false;
        return id.equals(level.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
