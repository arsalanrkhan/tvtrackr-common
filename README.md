# tvtrackr-common

Shared Maven library for the [TVTrackr](https://github.com/arsalanrkhan) microservices backend. Published to GitHub Packages and consumed by every service (`auth-service`, `gateway`, and future services) so cross-cutting concerns — error handling and Redis access — aren't reimplemented per service.

## What's in it

### Error handling (`com.tvtrackr.common.error`)

- **`BusinessErrors`** — base class for structured, service-specific error codes. Each subclass (e.g. `AuthErrors`, `GatewayErrors`) sets its own two-letter `prefix` (`AU`, `GW`, ...) and defines error constants as `code + desc + httpStatus`. Includes a generic `GENERIC` (`BE0000`) fallback.
- **`BusinessException`** — a `RuntimeException` wrapping a `BusinessErrors` instance, thrown by service code when a business rule is violated.
- **`ErrorResponseDTO`** — the standard JSON error shape returned to clients: `code`, `desc`, `timestamp`.
- **`GlobalExceptionHandler`** — a `@RestControllerAdvice` registered automatically (see below) that translates:
  - `BusinessException` → its mapped HTTP status and `ErrorResponseDTO`
  - `MethodArgumentNotValidException` (bean validation failures) → `400` with the first field error message
  - `HttpMessageNotReadableException` (malformed JSON body) → `400 BE0001`
  - any other uncaught `Exception` → `500 BE0000`

### Redis (`com.tvtrackr.common.redis`)

- **`RedisConfig`** — defines a `RedisTemplate<String, Object>` (Jackson JSON serialization for values, String serialization for keys) and a `StringRedisTemplate`.
- **`RedisService`** (interface) / **`RedisServiceImpl`** — a generic Redis wrapper used by all services:
  - `save` / `get` — object values with a TTL (milliseconds)
  - `saveString` / `getString` / `getAndDeleteString` — string values; `getAndDeleteString` is an atomic `GETDEL`, used for one-time tokens (password reset, email verification) so consumption and invalidation happen in a single round trip
  - `saveIfAbsent` / `saveStringIfAbsent` — atomic `SET NX`, used for cooldown enforcement to avoid a check-then-set race
  - `saveStringPermanent` — sets a key with no TTL, for permanent flags (e.g. a Bloom filter "seeded" guard)
  - `setBit` / `getBit` — single Redis bitmap operations
  - `setBitsPipelined` / `getBitsPipelined` — set or read multiple bit offsets in one pipelined round trip instead of *k* serial calls; used by the Bloom filter implementation in `auth-service`

## Auto-configuration

`RedisConfig`, `RedisServiceImpl`, and `GlobalExceptionHandler` are registered via `META-INF/spring/org.springframework.boot.autoconfigure/AutoConfiguration.imports`, so any service that adds this library as a dependency gets them wired in automatically — no explicit `@Import` or component scan needed.

## Notable dependency choice

`spring-boot-starter-web` is declared with `<scope>provided</scope>` — it's needed to compile (`@RestControllerAdvice`, `ResponseEntity`, etc.) but isn't pulled transitively into consuming services, so each service brings its own web stack (`gateway` uses `spring-cloud-starter-gateway-server-webmvc`, `auth-service` uses `spring-boot-starter-webmvc`) without conflicts.

## Tech

- Java 25, Spring Boot 4.0.6 (dependency management only — this is a plain library, not a Spring Boot app)
- Spring Data Redis, Jackson (`tools.jackson`), Lombok

## Publishing

`.github/workflows/publish.yml` runs on every push to `main`: sets up JDK 25, then `mvn deploy`s to GitHub Packages (`https://maven.pkg.github.com/arsalanrkhan/tvtrackr-common`), authenticated with the workflow's own `GITHUB_TOKEN`.

## Using it in a service

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/arsalanrkhan/tvtrackr-common</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.tvtrackr</groupId>
    <artifactId>common</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Requires GitHub Packages read auth configured in the consuming project's `~/.m2/settings.xml` (or CI credentials) since this is a private/public-but-authenticated Maven registry.
