# Architecture Guide

## Current Architecture

Auth is a single-module Gradle service.

- Technology: Java 21, Spring Boot 4.0.5
- Main package: `com.example.auth`
- Main responsibility: Application bootstrap only.
- Security: No security configuration visible.

## Layer Structure

- Controller layer: No controller package or route annotations found.
- Service layer: No service classes found.
- Repository layer: No repository classes found.
- DTO layer: No DTO classes found.
- Entity/domain layer: No entity classes found.
- Exception handling: No project-specific exception handling is visible.

## Layer Responsibilities

Controllers should focus on HTTP request and response handling.
Services should contain business logic.
Repositories should handle persistence.
Entities should not be returned directly as API responses.
Request DTOs and Response DTOs should be separated.
Existing architecture must not be changed without explicit instruction.
If API behavior changes, API_SPEC.yaml must be updated in the same PR.

## Transaction Boundary

Transaction boundaries are defined in service classes when `@Transactional` is present. If a service has no transaction annotations, transaction policy is Needs confirmation.

## API Documentation Responsibility

`API_SPEC.yaml` is the source of truth for the service API contract. Any API behavior change must update `API_SPEC.yaml` in the same PR.

## Forbidden Patterns

- Do not move business logic into controllers.
- Do not perform persistence directly from controllers.
- Do not expose JPA entities as API responses when response DTOs exist.
- Do not introduce a different architecture without explicit approval.
- Do not add cross-service assumptions that are not visible from code.
- Do not change build files, dependencies, or application configuration as part of ordinary feature work unless explicitly approved.
