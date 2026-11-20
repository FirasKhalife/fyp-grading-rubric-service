package com.fypgrading.rubricservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fypgrading.rubricservice.common.security.TestTokens;
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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
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
        copies.replace(session, java.time.Instant.now());
    }

    static JwtRequestPostProcessor as(String role) {
        return jwt().jwt(j -> j.subject(UUID.randomUUID().toString())
                        .claim("realm_access", Map.of("roles", List.of(role)))
                        .claim("name", "Rania Haddad"))
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
                .andExpect(jsonPath("$.criteria.length()").value(2))
                // Plan 3.4: who saved it, from the token.
                .andExpect(jsonPath("$.updatedBy.id").isNotEmpty())
                .andExpect(jsonPath("$.updatedBy.displayName").value("Rania Haddad"));

        // Review M16: a criterion id from this copy is unknown in another copy.
        UUID other = UUID.randomUUID();
        mvc.perform(put("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", other)
                        .with(as("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"criteria": [{"id": "%s", "name": "Hijack", "weight": 100,
                                  "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}}]}""".formatted(slidesId)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("unknown-criterion"));
        mvc.perform(get("/api/rubrics/PROPOSAL_PRESENTATION")
                        .header("X-Demo-Session", session)
                        .with(as("ADMIN")))
                .andExpect(jsonPath("$.criteria[0].name").value("Slides quality"));
    }

    /** Review low: weights are whole numbers and required. */
    @Test
    void aWeightMustBeAWholeNumber() throws Exception {
        for (String weight : List.of("33.5", "null")) {
            mvc.perform(put("/api/rubrics/PROPOSAL_PRESENTATION")
                            .header("X-Demo-Session", session)
                            .with(as("ADMIN"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"criteria": [{"name": "A", "weight": %s,
                                      "descriptions": {"4": "a", "3": "b", "2": "c", "1": "d"}}]}""".formatted(weight)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("validation-failed"));
        }
    }

    @Test
    void unknownAssessmentIsABadRequest() throws Exception {
        mvc.perform(get("/api/rubrics/THESIS").header("X-Demo-Session", session).with(as("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation-failed"));
    }

    @DynamicPropertySource
    static void realTokenKeys(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", TestTokens::jwkSetUri);
    }

    /** A signed token through the real decoder (not the jwt() shortcut), as the gateway forwards it. */
    private ResultActions withToken(String token) throws Exception {
        return mvc.perform(get("/api/rubrics/PROPOSAL_PRESENTATION")
                .header("X-Demo-Session", session)
                .header("Authorization", "Bearer " + token));
    }

    @Test
    void aSignedTokenFromOurIssuerToOurClientIsAccepted() throws Exception {
        int status = withToken(TestTokens.token(TestTokens.ISSUER, TestTokens.CLIENT, "ADMIN"))
                .andReturn()
                .getResponse()
                .getStatus();
        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void aTokenFromAnotherIssuerIsRejected() throws Exception {
        withToken(TestTokens.token("http://elsewhere.test/realms/assay", TestTokens.CLIENT, "ADMIN"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthenticated"));
    }

    @Test
    void aTokenIssuedToAnotherClientIsRejected() throws Exception {
        withToken(TestTokens.token(TestTokens.ISSUER, "admin-cli", "ADMIN"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("unauthenticated"));
    }
}
