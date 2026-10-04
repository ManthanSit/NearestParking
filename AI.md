# AI-assisted development

## Setup and context

Codex was used as the coding assistant. It had the project guidance in `AGENTS.md`, which sets the Java 27 and Spring Boot 4.1.1 baseline, asks for tests before implementation, and requires small changes and honest verification reports. No additional skills, reusable prompts, or custom agent rules were used.

## Division of work

I made the product and architecture decisions, ran Docker and Maven commands. Codex made the project and test changes, updated project documentation, and analyzed the reported results. Work stayed in one Codex session; no subagents or parallel delegation were used because the changes were sequential and small.

## Verification

Verification used focused checks rather than reading every line of every dependency or generated file, but I did check the main code parts myself and validation from swagger. The checks included Maven tests and packaging, OpenSpec validation, and the focused Redis and live Data.gov.sg integration tests. The live Data.gov.sg test passed when explicitly enabled.

## Issue found and corrected

1. Codex designed the application in 2 parts, one for the catalog fetching and other for the api and availability scheduler. It would start 2 instances of application from docker compose. Corrected and moved to a single application.
2. Distance was again getting calculated after we fetch the car parks in a kms range form redis, removed all that code to use the distance which redis is already calculating and sorting for us by distance.
3. Health indicator was not accurate, it would just check if availability and catalog exists and not the freshness of availability.

## What was not delegated to AI

I retained control of local machine setup, Docker access, and the product decisions. GitHub and other MCP access wasn't given to codex.
