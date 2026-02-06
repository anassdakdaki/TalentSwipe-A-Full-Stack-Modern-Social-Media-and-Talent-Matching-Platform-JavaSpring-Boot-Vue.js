# Testing (Biblo)

## Run
- Backend tests: `cd biblov1 && ./mvnw test` (or `mvn test` if Maven is installed)

## What is covered
- JWT auth:
  - register + login flow
  - protected endpoint rejects missing/invalid bearer token
  - protected endpoint allows valid bearer token
- Mutual-like matching:
  - match created only on mutual `LIKE`
  - no duplicate match rows for the same pair (normalized ordering)
  - no duplicate swipe rows per `(swiper, swiped)` pair
- Chat authorization:
  - non-participant cannot send messages to another pair’s room (403)
  - matched users can send and read messages

## Test configuration
- Profile: `test` via `@ActiveProfiles("test")`
- Config file: `biblov1/src/test/resources/application-test.yml`
- Database: H2 in-memory (`create-drop`)
- JWT secret: test-only Base64 key configured in the test profile

