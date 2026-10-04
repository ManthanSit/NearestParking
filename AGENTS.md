# Project working instructions

## Project scope

- This repository is the foundation for a nearest available car park service.
- Prefer straightforward Spring Boot conventions and a clear separation between API, application logic, configuration, and infrastructure code as those layers are added.
- Do not add dependencies unless the feature needs them.

## Technology baseline

- Use Java 27 and Spring Boot 4.1.1, as declared in `pom.xml`.
- Keep runtime configuration externalized through Spring properties and environment variables. Do not commit credentials or environment-specific secrets.
- Keep the application containerized with the existing Dockerfile and Compose setup. Preserve a non-root runtime user and a persistent named Redis volume.

## Verification

- Define and add the relevant test functionality before implementing the production behavior, so the test can expose problems early.
- For Java changes, run `mvn test` and `mvn package` when the required JDK and dependency access are available.
- For Docker Compose changes, run `docker compose config --quiet`.
- For container or runtime changes, build and start the Compose stack when a Docker daemon is available, then check the Actuator health endpoints.
- Report checks that could not run and the specific environment limitation; do not claim an unrun check passed.

## Editing and communication

- Make the smallest precise code change that satisfies the request; avoid unrelated edits.
- Make one small, reviewable change at a time and summarize what changed and how it was checked.
- Keep comments short and precise. Add longer comments only when explicitly requested.
- Write clear, complete OpenSpec specifications and Swagger/OpenAPI documentation for project features and API changes.
- Update the README when commands, configuration variables, endpoints, or service behavior change.
- Avoid tests, services, or production features unrelated to the requested change.
