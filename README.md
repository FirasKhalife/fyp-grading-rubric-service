# rubric-service

The grading grid of each of the five assessments:

- the criteria and their weights in %
- what the work looks like at levels 4 to 1 (level 0, "Not demonstrated", needs no description)

This service is part of **Assay**, a final-year-project grading platform. The architecture is described in
[fyp-grading-docker-compose/docs/architecture.md](https://github.com/FirasKhalife/fyp-grading-docker-compose/blob/master/docs/architecture.md).

## API

| Method and path | Who | What |
|---|---|---|
| `GET /api/rubrics` | every role | All five rubrics, in checkpoint order |
| `GET /api/rubrics/{assessment}` | every role | One rubric |
| `PUT /api/rubrics/{assessment}` | admin | Replace the criteria, in order. Ids are kept. The weights must add up to 100 |

## Events and calls

No messaging. evaluation-service reads a rubric when a draft starts, and keeps its own snapshot.

## Rules it enforces

- The weights of one rubric add up to exactly 100. Otherwise the save fails with 422 `weights-not-100` and the
  current total.
- A rubric has 1 to 20 criteria, each with a description for levels 1 to 4.

## Run it

The whole stack runs from [fyp-grading-docker-compose](https://github.com/FirasKhalife/fyp-grading-docker-compose).
To run this service on its own (Java 25, Maven):

```bash
./mvnw spring-boot:run
```

It needs Postgres (`rubric-db`) and Keycloak.

## Test

```bash
./mvnw test
```

`RubricApiTest` runs on a real Postgres. It covers:

- reading, editing, reordering and removing criteria
- the 100% rule and the roles
- the demo-session header, and isolation between copies

`SeedFileTest` checks the shipped seed. Docker must be running for the Testcontainers tests.

## Notes for reviewers

- Spring Boot 4.0 and Spring Cloud 2025.1 on Java 25. The image is layered and runs as a non-root user.
- Every call needs a Keycloak token and an `X-Demo-Session` header.
- Each visitor gets a private copy of the data:
  - Every row is scoped to the visitor's session (Hibernate `@TenantId`).
  - A visitor's copy is created from `src/main/resources/demo/seed.json`.
  - Every copy is deleted at midnight (Europe/Paris).
- Errors are RFC 9457 problem details with a stable `code`.
- Roles are checked with method security. Identity always comes from the token.
- Actuator exposes only health, info and prometheus, and the gateway doesn't route it.
- The shared API contract lives in the compose repo ([`contract/openapi.yaml`](https://github.com/FirasKhalife/fyp-grading-docker-compose/blob/master/contract/openapi.yaml)). This service publishes its own part
  at `/v3/api-docs`.

## Lint and CI

- **Formatting:** Spotless with palantir-java-format. `./mvnw spotless:apply` fixes it.
- **Lint:** Checkstyle (`checkstyle.xml`) catches likely bugs and sloppy code.
- The build checks both in the `validate` phase, so `./mvnw verify` runs the lint, the build and every test.
- **GitHub Actions** (`.github/workflows/ci.yml`) runs on every pull request and on pushes to `master`:
  - the lint
  - `verify`, including the Testcontainers tests
  - the Docker image: built, then scanned with Trivy (the report goes to GitHub code scanning; a HIGH or CRITICAL
    finding with a fix fails the build). From `master` it is pushed to `ghcr.io/firaskhalife/fyp-grading-rubric-service`
    (tags `latest` and the short commit SHA) with an SBOM and build provenance, and signed with cosign
- **Dependabot** proposes weekly updates for Maven, the Dockerfile's base images and the GitHub Actions, all pinned
  (base images by digest, actions by commit SHA).

The `Dockerfile` (the same in every service repo) builds a small Java 25 runtime with `jlink` on Alpine, runs as
an unprivileged user, and ships an AOT cache made by a training run at build time, so the service starts in
about half the time. Accepted unfixable scan findings, if any, are listed in `.trivyignore` with a reason.
