package com.fypgrading.rubricservice.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LevelDTO {

    @NotNull
    private String name;

    @NotNull
    private Integer weight;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LevelDTO levelDTO)) return false;
        return name.equals(levelDTO.getName());
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
