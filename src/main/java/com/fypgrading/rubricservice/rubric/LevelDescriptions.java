package com.fypgrading.rubricservice.rubric;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** What the work looks like at each level, keyed "4" to "1" in JSON. Level 0 needs no description. */
public record LevelDescriptions(
        @JsonProperty("4") @NotBlank @Size(max = 1000) String exemplary,
        @JsonProperty("3") @NotBlank @Size(max = 1000) String accomplished,
        @JsonProperty("2") @NotBlank @Size(max = 1000) String developing,
        @JsonProperty("1") @NotBlank @Size(max = 1000) String beginning) {}
