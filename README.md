# FitTrack – Fitness Activity Tracker API

A REST API built with **Java 21 and Spring Boot 4** for logging workouts, estimating calories burned, and tracking progress with stats and workout streaks. It includes a small web dashboard and interactive API docs.

**Live demo:** _LIVE_URL_ &nbsp;|&nbsp; **API docs (Swagger):** _LIVE_URL_/swagger-ui.html

> Hosted on a free tier: the first request after a period of inactivity can take up to a minute while the server wakes up.

## Features

- **Users and activities:** full CRUD with pagination, sorting (newest first) and filtering by activity type
- **Calorie estimation:** if the client does not send calories, they are estimated with the MET formula (`MET × weight kg × hours`)
- **Stats:** totals and a per-type breakdown for any date range (defaults to the last 30 days)
- **Streaks:** current and longest streak of consecutive active days (see `StreakCalculator`)
- **Caching:** stats are cached with Caffeine (10 min TTL) and evicted whenever activities change
- **Validation and error handling:** Bean Validation on all inputs, with consistent [RFC 9457 Problem Details](https://www.rfc-editor.org/rfc/rfc9457) error responses including per-field messages
- **Tests:** 22 unit and integration tests (JUnit 5, Mockito, MockMvc)
- **DevOps:** multi-stage Dockerfile, GitHub Actions CI, health checks via Spring Boot Actuator, one-click deployment to Render

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 (Web MVC, Data JPA, Validation, Cache, Actuator) |
| Database | PostgreSQL (production), H2 in-memory (local and tests) |
| ORM | Hibernate / Spring Data JPA |
| Caching | Caffeine |
| API docs | springdoc-openapi (Swagger UI) |
| Testing | JUnit 5, Mockito, MockMvc, AssertJ |
| Build / CI | Maven, GitHub Actions |
| Deployment | Docker, Render |

## Architecture

Code is organized **by feature**. Each feature has a controller → service → repository layering:

```
com.hamsa.fittrack
├── user/       User entity, repository, service, REST controller, DTOs
├── activity/   Activity entity, repository, service, controller, CalorieCalculator
├── stats/      StatsService (aggregation + caching), StreakCalculator
├── common/     Global exception handler, custom exceptions, PageResponse
└── config/     Clock + OpenAPI beans, demo data loader
```

- **DTOs (Java records)** keep the API contract separate from JPA entities
- **A `Clock` is injected** instead of calling `LocalDate.now()` directly, so date logic is testable
- **Database index** on `(user_id, performed_at)`, because almost every activity query filters by user and time
- The streak query loads only timestamps rather than full entities

## API endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/users` | Create a user |
| GET | `/api/users` | List users |
| GET | `/api/users/{id}` | Get a user |
| DELETE | `/api/users/{id}` | Delete a user and their activities |
| POST | `/api/users/{userId}/activities` | Log an activity |
| GET | `/api/users/{userId}/activities?type=&page=&size=` | List activities (paginated) |
| GET | `/api/activities/{id}` | Get an activity |
| PUT | `/api/activities/{id}` | Update an activity |
| DELETE | `/api/activities/{id}` | Delete an activity |
| GET | `/api/users/{userId}/stats?from=&to=` | Stats and streaks for a date range |

Example:

```bash
curl -X POST http://localhost:8080/api/users/1/activities \
  -H "Content-Type: application/json" \
  -d '{"type": "RUNNING", "durationMinutes": 30, "distanceKm": 5}'
```

```json
{"id": 19, "userId": 1, "type": "RUNNING", "durationMinutes": 30, "distanceKm": 5.0,
 "caloriesBurned": 343, "performedAt": "2026-09-18T21:10:00", "notes": null}
```

## Running locally

Requires Java 21+. No database setup is needed because it uses in-memory H2 with demo data.

```bash
./mvnw spring-boot:run
```

- Dashboard: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Run the tests:

```bash
./mvnw test
```

Run with PostgreSQL (the `prod` profile):

```bash
SPRING_PROFILES_ACTIVE=prod DB_HOST=localhost DB_NAME=fittrack DB_USER=postgres DB_PASSWORD=secret ./mvnw spring-boot:run
```

## Deployment

The app is containerized with a multi-stage Dockerfile (JDK image for the build, smaller JRE image running as a non-root user). `render.yaml` is a Render Blueprint that creates the web service and a PostgreSQL database and wires up the connection through environment variables.

## Possible improvements

- Authentication with Spring Security + JWT so users can only access their own data
- Flyway migrations instead of `ddl-auto=update`
- Redis as a distributed cache when running multiple instances
- Rate limiting on write endpoints
- Weekly goals and notifications
