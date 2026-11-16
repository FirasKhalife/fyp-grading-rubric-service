package com.fypgrading.rubricservice;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** The shipped demo seed (not the small test seed) is a valid set of rubrics. */
class SeedFileTest {

    @Test
    void everyRubricHasFiveAssessmentsWorthOfCriteriaAddingUpTo100() {
        JsonNode seed = JsonMapper.builder()
                .build()
                .readTree(Path.of("src/main/resources/demo/seed.json").toFile());
        assertThat(seed.get("rubrics")).hasSize(5);
        for (JsonNode rubric : seed.get("rubrics")) {
            int total = 0;
            for (JsonNode c : rubric.get("criteria")) {
                total += c.get("weight").asInt();
                assertThat(c.get("descriptions").propertyNames()).containsExactlyInAnyOrder("1", "2", "3", "4");
            }
            assertThat(total).as(rubric.get("assessment").asString()).isEqualTo(100);
        }
    }
}
