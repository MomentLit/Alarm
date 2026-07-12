# Service Overview

## Service Name

Alarm

## Service Responsibility

Alarm creation for matching events. Other services call this API to store an alarm for the matching host, and the receiver is resolved from the Matching service rather than from the caller's token.

## Technology Stack

- Language: Java 21
- Framework: Spring Boot 4.1.0
- Build Tool: Gradle
- Database: PostgreSQL via Spring Data JPA; H2 test runtime dependency
- Other: Lombok is used where visible. JWT (jjwt) and Spring Security protect `/alarm/**`. `RestClient` is used for internal service calls.

## Main Package Structure

- Main package: `com.example.alarm`
- Controller: AlarmController handles the visible /alarm API.
- Service: AlarmService resolves the matching host through MatchingClient and persists the alarm. The external lookup runs outside any transaction.
- Repository: AlarmRepository is the persistence boundary.
- Client: MatchingClient calls the Matching service internal API (`GET /internal/matchings/{matchingId}`) with connect/read timeouts and converts call failures to MatchingClientException. MatchingResponse and MatchingStatus live in `client.dto`.
- DTO: AlarmCreateRequest is used for creation. ApiResponse<T> built through ResponseUtil wraps controller responses.
- Entity/domain: Alarm is a JPA entity mapping the `Matchings_Alarm` table. Role enum exists for security principal roles.

## Main Domains

Alarm entity storing userId (the matching host, varchar), matchingId, and description by value. MatchingStatus (REQUESTED, APPROVED, REJECTED, CANCELED) exists only as a client DTO field.

## Main Features

Alarm creation for a matching. The alarm receiver is the matching `host_id` fetched from the Matching service, not the authenticated caller.

## Main APIs

Visible API: `POST /alarm/{matching_id}`. Full details are in API_SPEC.yaml. `PATCH /alarm/{matching_id}` (alarmReadUpdate) exists in API_SPEC.yaml but is not implemented yet.

## Data Access Structure

AlarmRepository extends JpaRepository<Alarm, Long>. No custom query methods are visible. Matching data is not read from the database; it comes from the Matching service internal API.

## Exception Handling

GlobalExceptionHandler (`@RestControllerAdvice`) returns `ApiResponse.fail("[ERROR: Domain/Type] message")`:

- MethodArgumentNotValidException -> 400 Bad Request
- MatchingClientException -> 502 Bad Gateway
- Any other Exception -> 500 Internal Server Error

Authentication failures are handled by Spring Security and return 403 with an empty body.

## Test Structure

Controller (MockMvc with real JWT tokens and a mocked MatchingClient), service (Mockito), repository (@DataJpaTest on H2), and application context tests are present under com.example.alarm.

## API Documentation

This service uses `API_SPEC.yaml` as the main API specification.
When API behavior changes, `API_SPEC.yaml` must be updated in the same PR.

## Development Notes

- Preserve the current single-module service structure.
- Follow the existing package and naming conventions.
- Keep controller, service, repository, client, entity, and DTO responsibilities separate where those layers exist.
- Keep external HTTP calls outside transactions.
- Do not add cross-service behavior unless it is visible in code or explicitly specified by an Issue.
- If implementation changes API behavior, update `API_SPEC.yaml` in the same PR.

## Needs Confirmation

- MatchingClient timeout values (connect 3s, read 5s) are defaults without a documented SLA.
- The Matching service internal API contract (`/internal/matchings/{matchingId}` response fields) is confirmed verbally but not documented in a shared spec.
- `PATCH /alarm/{matching_id}` (alarmReadUpdate) is specified but not implemented; its policy is undefined.
- Alarm list/read APIs for end users are not defined.
