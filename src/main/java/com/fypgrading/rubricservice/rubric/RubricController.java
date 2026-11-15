package com.fypgrading.rubricservice.rubric;

import com.fypgrading.rubricservice.rubric.RubricDtos.RubricPut;
import com.fypgrading.rubricservice.rubric.RubricDtos.RubricView;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** P7: every role reads rubrics (students too); only the coordinator edits them. */
@RestController
@RequestMapping("/api/rubrics")
public class RubricController {

    private final RubricService rubrics;

    public RubricController(RubricService rubrics) {
        this.rubrics = rubrics;
    }

    @GetMapping
    public List<RubricView> list() {
        return rubrics.all();
    }

    @GetMapping("/{assessment}")
    public RubricView get(@PathVariable Assessment assessment) {
        return rubrics.one(assessment);
    }

    @PutMapping("/{assessment}")
    @PreAuthorize("hasRole('ADMIN')")
    public RubricView replace(@PathVariable Assessment assessment, @Valid @RequestBody RubricPut body) {
        return rubrics.replace(assessment, body);
    }
}
