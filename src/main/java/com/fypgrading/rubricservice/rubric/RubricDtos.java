package com.fypgrading.rubricservice.rubric;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
            Instant updatedAt,
            Editor updatedBy) {}

    /** Who last saved the rubric. */
    public record Editor(UUID id, String displayName) {}

    public record CriterionView(UUID id, int position, String name, int weight, LevelDescriptions descriptions) {}

    public record LevelView(int value, String name) {}

    public record RubricPut(
            @NotNull @Size(min = 1, max = 20) List<@Valid @NotNull CriterionPut> criteria) {}

    /** {@code weight} is required and a whole number (33.5 is refused, not truncated). */
    public record CriterionPut(
            UUID id,
            @NotBlank @Size(max = 300) String name,

            @NotNull @Digits(integer = 3, fraction = 0) @DecimalMin("0") @DecimalMax("100")
            java.math.BigDecimal weight,

            @NotNull @Valid LevelDescriptions descriptions) {

        public int weightPercent() {
            return weight.intValueExact();
        }
    }

    public static final List<LevelView> SCALE = List.of(
            new LevelView(4, "Exemplary"),
            new LevelView(3, "Accomplished"),
            new LevelView(2, "Developing"),
            new LevelView(1, "Beginning"),
            new LevelView(0, "Not demonstrated"));
}
