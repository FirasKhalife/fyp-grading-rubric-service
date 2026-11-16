package com.fypgrading.rubricservice;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fypgrading.rubricservice.demo.DemoCopyService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class RubricApiTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    MockMvc mvc;

    @Autowired
    DemoCopyService copies;

    UUID session = UUID.randomUUID();

    @BeforeEach
    void seedCopy() {
        copies.replace(session);
    }

    static JwtRequestPostProcessor as(String role) {
        return jwt().jwt(j ->
                        j.subject(UUID.randomUUID().toString()).claim("realm_access", Map.of("roles", List.of(role))))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Test
    void everyRoleReadsTheRubricsOfItsOwnCopy() throws Exception {
        mvc.perform(get("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criteria.length()").value(2))
                .andExpect(jsonPath("$.totalWeight").value(100))
                .andExpect(jsonPath("$.criteria[0].descriptions.4").value("All objectives."))
                .andExpect(jsonPath("$.levels[4].name").value("Not demonstrated"));

        mvc.perform(get("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", UUID.randomUUID())
                        .with(as("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criteria.length()").value(0));
    }

    @Test
    void requestsNeedATokenAndADemoSession() throws Exception {
        mvc.perform(get("/api/rubrics").header("X-Demo-Session", session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthenticated"));
        mvc.perform(get("/api/rubrics").with(as("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("demo-session-required"));
        mvc.perform(get("/api/rubrics").header("X-Demo-Session", "not-a-uuid").with(as("ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void onlyTheCoordinatorEditsAndWeightsMustAddUpTo100() throws Exception {
        String body = """
                {"criteria": [
                  {"name": "Objectives", "weight": 50, "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}},
                  {"name": "Slides", "weight": 45, "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}}
                ]}""";
        mvc.perform(put("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("REVIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("forbidden"));
        mvc.perform(put("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("weights-not-100"))
                .andExpect(jsonPath("$.totalWeight").value(95));
    }

    @Test
    void editingKeepsIdsReordersAndRemoves() throws Exception {
        String json = mvc.perform(get("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("ADMIN")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String slidesId = com.jayway.jsonpath.JsonPath.read(json, "$.criteria[1].id");
        String body = """
                {"criteria": [
                  {"id": "%s", "name": "Slides quality", "weight": 30, "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}},
                  {"name": "Demo", "weight": 70, "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}}
                ]}""".formatted(slidesId);
        mvc.perform(put("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.criteria[0].id").value(slidesId))
                .andExpect(jsonPath("$.criteria[0].position").value(1))
                .andExpect(jsonPath("$.criteria[1].name").value("Demo"))
                .andExpect(jsonPath("$.criteria.length()").value(2));
    }

    @Test
    void unknownAssessmentIsABadRequest() throws Exception {
        mvc.perform(get("/api/rubrics/THESIS").header("X-Demo-Session", session).with(as("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation-failed"));
    }
}
