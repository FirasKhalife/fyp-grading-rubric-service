package com.fypgrading.rubricservice.rubric;

import com.fypgrading.rubricservice.common.jpa.AssignedIdEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

/** One row of a rubric: a criterion, its weight in %, and what the work looks like at levels 4 to 1. */
@Entity
@Table(name = "criterion")
public class Criterion extends AssignedIdEntity {

    @TenantId
    @Column(name = "demo_session", nullable = false, updatable = false)
    private String demoSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Assessment assessment;

    @Column(nullable = false)
    private int position;

    @Column(nullable = false, length = 300)
    private String name;

    @Column(nullable = false)
    private int weight;

    @Column(name = "level4", nullable = false, length = 1000)
    private String level4;

    @Column(name = "level3", nullable = false, length = 1000)
    private String level3;

    @Column(name = "level2", nullable = false, length = 1000)
    private String level2;

    @Column(name = "level1", nullable = false, length = 1000)
    private String level1;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Criterion() {}

    public Criterion(UUID id, Assessment assessment) {
        super(id);
        this.assessment = assessment;
    }

    public void update(int position, String name, int weight, LevelDescriptions descriptions, Instant at) {
        this.position = position;
        this.name = name;
        this.weight = weight;
        this.level4 = descriptions.exemplary();
        this.level3 = descriptions.accomplished();
        this.level2 = descriptions.developing();
        this.level1 = descriptions.beginning();
        this.updatedAt = at;
    }

    public Assessment getAssessment() {
        return assessment;
    }

    public int getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public LevelDescriptions getDescriptions() {
        return new LevelDescriptions(level4, level3, level2, level1);
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
