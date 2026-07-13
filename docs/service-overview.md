# Service Overview

## Service Name

Alarm

## Service Responsibility

Alarm creation, listing, and read-status update for matching events. Other services call the creation API to store an alarm for the matching host, and the receiver is resolved from the Matching service rather than from the caller's token. End users list their own alarms and mark a single alarm as read using their token.

## Technology Stack

- Language: Java 21
- Framework: Spring Boot 4.1.0
- Build Tool: Gradle
- Database: PostgreSQL via Spring Data JPA; H2 test runtime dependency
- Other: Lombok is used where visible. JWT (jjwt) and Spring Security protect `/alarm/**`. `RestClient` is used for internal service calls.

## Main Package Structure

- Main package: `com.example.alarm`
- Controller: AlarmController handles the visible /alarm APIs (create, list, read update). The authenticated user is taken from `UserPrincipal` via `@AuthenticationPrincipal` for list and read update.
- Service: AlarmService resolves the matching host through MatchingClient and persists the alarm. The external lookup runs outside any transaction. It also lists the caller's alarms as AlarmResponse and marks a single owned alarm as read inside a `@Transactional` method using entity dirty checking.
- Repository: AlarmRepository is the persistence boundary.
- Client: MatchingClient calls the Matching service internal API (`GET /internal/matchings/{matchingId}`) with connect/read timeouts and converts call failures to MatchingClientException. MatchingResponse and MatchingStatus live in `client.dto`.
- DTO: AlarmCreateRequest is used for creation. AlarmResponse (`dto.response`) is used for the alarm list. ApiResponse<T> built through ResponseUtil wraps controller responses.
- Entity/domain: Alarm is a JPA entity mapping the `Matchings_Alarm` table. Role enum exists for security principal roles.

## Main Domains

Alarm entity storing userId (the matching host, varchar), matchingId, description, and isRead (`is_read`, defaults to false on creation, set to true by `markAsRead()`). MatchingStatus (REQUESTED, APPROVED, REJECTED, CANCELED) exists only as a client DTO field.

## Main Features

- Alarm creation for a matching. The alarm receiver is the matching `host_id` fetched from the Matching service, not the authenticated caller.
- Alarm list for the authenticated user, ordered by id descending.
- Read update for a single alarm by alarm id. Only the caller's own alarm is updated; a missing or non-owned alarm id is a no-op that still returns 200.

## Main APIs

Visible APIs: `POST /alarm/{matching_id}`, `GET /alarm`, `PATCH /alarm/{alarm_id}` (alarmReadUpdate). Full details are in API_SPEC.yaml.

## Data Access Structure

AlarmRepository extends JpaRepository<Alarm, Long> with derived queries `findAllByUserIdOrderByIdDesc(userId)` and `findByIdAndUserId(id, userId)`. Matching data is not read from the database; it comes from the Matching service internal API.

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
- The read update no-op policy (200 for a missing or non-owned alarm id) may later need explicit 404/403 handling if the team defines an error policy.
