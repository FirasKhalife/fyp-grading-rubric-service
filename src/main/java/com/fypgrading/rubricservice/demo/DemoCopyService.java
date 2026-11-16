package com.fypgrading.rubricservice.demo;

import com.fypgrading.rubricservice.common.demo.DemoSession;
import com.fypgrading.rubricservice.common.demo.SeedClock;
import com.fypgrading.rubricservice.common.demo.SeedIds;
import com.fypgrading.rubricservice.rubric.Assessment;
import com.fypgrading.rubricservice.rubric.Criterion;
import com.fypgrading.rubricservice.rubric.CriterionRepository;
import com.fypgrading.rubricservice.rubric.LevelDescriptions;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Each visitor's private copy of the rubrics: created from {@code demo/seed.json} when the visitor's demo
 * session starts, and deleted on reset and at midnight (Europe/Paris).
 */
@Service
public class DemoCopyService {

    private static final Logger log = LoggerFactory.getLogger(DemoCopyService.class);

    private final CriterionRepository criteria;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final Seed seed;

    public DemoCopyService(CriterionRepository criteria, TransactionTemplate tx, Clock clock, JsonMapper json)
            throws IOException {
        this.criteria = criteria;
        this.tx = tx;
        this.clock = clock;
        try (InputStream in = new ClassPathResource("demo/seed.json").getInputStream()) {
            this.seed = json.readValue(in, Seed.class);
        }
    }

    /** Deletes the session's copy, if any, and writes a fresh one. Idempotent. */
    public void replace(UUID session) {
        SeedClock seedClock = SeedClock.startingNow(clock);
        tx.executeWithoutResult(s -> criteria.deleteCopy(session.toString()));
        DemoSession.runAs(
                session,
                () -> tx.executeWithoutResult(s -> {
                    for (SeedRubric rubric : seed.rubrics()) {
                        for (SeedCriterion c : rubric.criteria()) {
                            Criterion criterion = new Criterion(SeedIds.of(session, c.key()), rubric.assessment());
                            criterion.update(c.position(), c.name(), c.weight(), c.descriptions(), seedClock.now());
                            criteria.save(criterion);
                        }
                    }
                }));
    }

    public void delete(UUID session) {
        tx.executeWithoutResult(s -> criteria.deleteCopy(session.toString()));
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    public void purgeAll() {
        Integer deleted = tx.execute(s -> criteria.deleteAllCopies());
        log.info("Midnight purge: deleted {} criteria across all demo copies", deleted);
    }

    record Seed(List<SeedRubric> rubrics) {}

    record SeedRubric(Assessment assessment, String name, List<SeedCriterion> criteria) {}

    record SeedCriterion(String key, int position, String name, int weight, LevelDescriptions descriptions) {}
}
