# Coding Convention

## Current Service Conventions

- Main package: `com.example.alarm`
- Controller structure: No controller package or route annotations found.
- Service structure: No service classes found.
- Repository structure: No repository classes found.
- DTO structure: No DTO classes found.
- Entity or domain structure: No entity classes found.
- Exception style: No project-specific exception handling is visible.

## Naming Conventions

- Controller classes use the `*Controller` suffix when controllers exist.
- Service classes use the `*Service` suffix when services exist.
- Repository interfaces use the `*Repository` suffix and extend Spring Data repository interfaces when persistence exists.
- Client classes use the `*Client` suffix and call other services over HTTP; client response DTOs live in `client.dto`.
- Request DTOs use the `*Request` suffix.
- Response DTOs use the `*Response` or `*Responses` suffix.
- Entity classes use domain nouns.
- Enum names use domain nouns such as role, status, or category.
- Method names should describe the use case or derived repository query in the existing style.
- Package names are lowercase and grouped by technical responsibility such as `controller`, `service`, `repository`, `entity`, `client`, `client.dto`, `dto.request`, `dto.response`, and `global` where those packages exist.

## Comment Rules

- Prefer clear code and small methods over explanatory comments.
- Add comments only when they clarify non-obvious business rules or constraints.
- Avoid stale comments that restate the method name.

## Required Principles

Follow the existing naming conventions.
Do not write code in a style that conflicts with the existing codebase.
Do not create unnecessary abstractions or shared utilities.
Do not modify unrelated files.
If API behavior changes, update API_SPEC.yaml.

## Forbidden Patterns

- Do not change package structure without explicit instruction.
- Do not add dependencies without explicit instruction.
- Do not return entities directly from API responses when DTOs exist.
- Do not mix controller, service, repository, and DTO responsibilities.
- Do not introduce speculative shared utilities.
- Do not modify unrelated source, build, dependency, or configuration files.
- Do not invent error response formats that are not implemented.
