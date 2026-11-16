package com.fypgrading.rubricservice.rubric;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Request and response bodies of the rubric API (contract: Rubric, Criterion, RubricPut). */
public final class RubricDtos {

    private RubricDtos() {}

    public record RubricView(
            Assessment assessment,
            String name,
            List<CriterionView> criteria,
            int totalWeight,
            List<LevelView> levels,
            Instant updatedAt) {}

    public record CriterionView(UUID id, int position, String name, int weight, LevelDescriptions descriptions) {}

    public record LevelView(int value, String name) {}

    public record RubricPut(
            @NotNull @Size(min = 1, max = 20) List<@Valid @NotNull CriterionPut> criteria) {}

    public record CriterionPut(
            UUID id,
            @NotBlank @Size(max = 300) String name,
            @Min(0) @Max(100) int weight,
            @NotNull @Valid LevelDescriptions descriptions) {}

    public static final List<LevelView> SCALE = List.of(
            new LevelView(4, "Exemplary"),
            new LevelView(3, "Accomplished"),
            new LevelView(2, "Developing"),
            new LevelView(1, "Beginning"),
            new LevelView(0, "Not demonstrated"));
}
