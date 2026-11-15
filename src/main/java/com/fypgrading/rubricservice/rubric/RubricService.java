package com.fypgrading.rubricservice.rubric;

import com.fypgrading.rubricservice.common.web.ApiException;
import com.fypgrading.rubricservice.rubric.RubricDtos.CriterionPut;
import com.fypgrading.rubricservice.rubric.RubricDtos.CriterionView;
import com.fypgrading.rubricservice.rubric.RubricDtos.RubricPut;
import com.fypgrading.rubricservice.rubric.RubricDtos.RubricView;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RubricService {

    private final CriterionRepository criteria;
    private final Clock clock;

    public RubricService(CriterionRepository criteria, Clock clock) {
        this.criteria = criteria;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RubricView> all() {
        Map<Assessment, List<Criterion>> byAssessment = criteria.findAllByOrderByPosition().stream()
                .collect(Collectors.groupingBy(Criterion::getAssessment));
        return Arrays.stream(Assessment.values())
                .map(a -> view(a, byAssessment.getOrDefault(a, List.of())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RubricView one(Assessment assessment) {
        return view(assessment, criteria.findByAssessmentOrderByPosition(assessment));
    }

    /**
     * Replaces the criteria list, in order. Criteria keep their id when sent with one; missing ones are
     * removed. Evaluations keep their own snapshot, so this never changes a submitted score.
     */
    @Transactional
    public RubricView replace(Assessment assessment, RubricPut body) {
        int total = body.criteria().stream().mapToInt(CriterionPut::weight).sum();
        if (total != 100) {
            throw ApiException.unprocessable("weights-not-100",
                    "Weights must add up to 100%. They add up to " + total + "%.").with("totalWeight", total);
        }
        List<Criterion> existing = criteria.findByAssessmentOrderByPosition(assessment);
        Map<UUID, Criterion> byId = existing.stream().collect(Collectors.toMap(Criterion::getId, Function.identity()));
        Set<UUID> seen = new HashSet<>();
        List<ApiException.FieldProblem> unknown = new ArrayList<>();
        for (int i = 0; i < body.criteria().size(); i++) {
            UUID id = body.criteria().get(i).id();
            if (id != null && (!byId.containsKey(id) || !seen.add(id))) {
                unknown.add(new ApiException.FieldProblem("criteria[" + i + "].id",
                        "is not a criterion of this rubric, or appears twice"));
            }
        }
        if (!unknown.isEmpty()) {
            throw new ApiException(org.springframework.http.HttpStatus.UNPROCESSABLE_CONTENT, "unknown-criterion",
                    "Some criteria don't belong to this rubric.", unknown);
        }

        Instant now = clock.instant();
        List<Criterion> kept = new ArrayList<>();
        for (int i = 0; i < body.criteria().size(); i++) {
            CriterionPut item = body.criteria().get(i);
            Criterion criterion = item.id() == null
                    ? new Criterion(UUID.randomUUID(), assessment)
                    : byId.get(item.id());
            criterion.update(i + 1, item.name().trim(), item.weight(), item.descriptions(), now);
            kept.add(criterion);
        }
        existing.stream().filter(c -> !seen.contains(c.getId())).forEach(criteria::delete);
        criteria.saveAll(kept);
        return view(assessment, kept);
    }

    private static RubricView view(Assessment assessment, List<Criterion> list) {
        List<CriterionView> items = list.stream()
                .sorted(Comparator.comparingInt(Criterion::getPosition))
                .map(c -> new CriterionView(c.getId(), c.getPosition(), c.getName(), c.getWeight(), c.getDescriptions()))
                .toList();
        Instant updatedAt = list.stream().map(Criterion::getUpdatedAt).filter(Objects::nonNull)
                .max(Comparator.naturalOrder()).orElse(null);
        return new RubricView(assessment, assessment.displayName(), items,
                items.stream().mapToInt(CriterionView::weight).sum(), RubricDtos.SCALE, updatedAt);
    }
}
