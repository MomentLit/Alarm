# Architecture Guide

## Current Architecture

Alarm is a single-module Gradle service.

- Technology: Java 21, Spring Boot 4.1.0
- Main package: `com.example.alarm`
- Main responsibility: Alarm creation API for matching events.
- Security: JWT Bearer authentication via `JwtFilter` and `SecurityConfig`. `/alarm/**` requires authentication; all other requests are permitted. Sessions are stateless.
- External dependency: Matching service internal API, called over HTTP from the `client` package.

## Layer Structure

- Controller layer: `controller` package. `AlarmController` handles `POST /alarm/{matching-id}`.
- Service layer: `service` package. `AlarmService` contains the alarm creation use case.
- Repository layer: `repository` package. `AlarmRepository` extends `JpaRepository`.
- Client layer: `client` package. `MatchingClient` calls the Matching service internal API (`GET /internal/matchings/{matchingId}`) using `RestClient`. Client response DTOs live in `client.dto`.
- DTO layer: `dto.request` package for request DTOs (`AlarmCreateRequest`). Common response wrapper is `global.dto.ApiResponse` built through `global.util.ResponseUtil`.
- Entity/domain layer: `entity` package. `Alarm` maps the `Matchings_Alarm` table.
- Exception handling: `global.exception.GlobalExceptionHandler` (`@RestControllerAdvice`) maps exceptions to `ApiResponse.fail` bodies.

## Layer Responsibilities

Controllers should focus on HTTP request and response handling.
Services should contain business logic.
Repositories should handle persistence.
Clients should only call external services and must not contain domain decisions.
Entities should not be returned directly as API responses.
Request DTOs and Response DTOs should be separated.
Existing architecture must not be changed without explicit instruction.
If API behavior changes, API_SPEC.yaml must be updated in the same PR.

## Transaction Boundary

Transaction boundaries are defined in service classes with `@Transactional` when a use case requires multiple persistence operations. External HTTP calls must stay outside transactions. `AlarmService.create` calls the Matching service without a transaction and delegates its single insert to `AlarmRepository.save`, which runs in the default Spring Data JPA repository transaction.

## Exception To HTTP Mapping

- `MethodArgumentNotValidException` -> 400 Bad Request
- `MatchingClientException` -> 502 Bad Gateway
- Any other `Exception` -> 500 Internal Server Error

## API Documentation Responsibility

`API_SPEC.yaml` is the source of truth for the service API contract. Any API behavior change must update `API_SPEC.yaml` in the same PR.

## Forbidden Patterns

- Do not move business logic into controllers.
- Do not perform persistence directly from controllers.
- Do not expose JPA entities as API responses when response DTOs exist.
- Do not introduce a different architecture without explicit approval.
- Do not add cross-service assumptions that are not visible from code.
- Do not change build files, dependencies, or application configuration as part of ordinary feature work unless explicitly approved.
