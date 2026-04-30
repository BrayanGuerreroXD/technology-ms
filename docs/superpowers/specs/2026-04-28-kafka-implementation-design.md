# Kafka Implementation Design — technology-ms

## Context

The `technology-ms` microservice needs bidirectional Kafka integration: it must consume login/logout auth events from other services and publish technology catalog sync events whenever a technology is created, updated, or deleted.

Stack: Spring Boot 4.0.5, Spring WebFlux, Java 25, Bancolombia Clean Architecture, MapStruct 1.6.3, spring-kafka.

---

## Architecture

Two new Gradle modules following hexagonal architecture conventions:

```
infrastructure/
├── entry-points/
│   ├── reactive-web/          (existing)
│   └── kafka-consumer/        (NEW) — inbound, triggers use cases
├── driven-adapters/
│   ├── r2dbc-repository/      (existing)
│   └── kafka-publisher/       (NEW) — outbound, driven by use cases
```

**Data flow — consume:**
```
Kafka topic → @KafkaListener → UseCase.method().subscribe() → Repository → MySQL
```

**Data flow — publish:**
```
HTTP → UseCase → Repository (save/update/delete) → TechnologyEventGateway.publish() → KafkaTemplate → Kafka topic
```

---

## Domain Changes (`domain/`)

### New gateway interface

**File:** `domain/model/src/main/java/co/com/technology/model/technology/gateways/TechnologyEventGateway.java`

Two methods: one for create/update (`sync.technologies.catalog`), one for delete (`sync.technologies.deleted`). Same DTO payload `{ id, name }` in both topics.

```java
public interface TechnologyEventGateway {
    Mono<Void> publish(Technology technology);        // → sync.technologies.catalog
    Mono<Void> publishDeleted(Technology technology); // → sync.technologies.deleted
}
```

### New use case: DeleteAuth

**Files:** `domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/`

- `DeleteAuthService.java` — interface with `Mono<Void> delete(String email, String token)`
- `DeleteAuthUseCase.java` — calls `authRepository.findByEmail(email)`, filters token match, calls `authRepository.deleteByEmail(email)`. No new gateway methods needed.

### Modified use cases (inject TechnologyEventGateway)

Publishing is **fire-and-forget**: it runs as a non-blocking side effect and does NOT affect the use case response or fail the HTTP flow if Kafka is unavailable.

| Use Case | Method | Pattern |
|---|---|---|
| `CreateTechnologyUseCase` | `eventGateway.publish()` | `technologyRepository.save(tech).doOnSuccess(saved -> eventGateway.publish(saved).subscribe())` |
| `UpdateTechnologyUseCase` | `eventGateway.publish()` | `technologyRepository.update(tech).doOnSuccess(saved -> eventGateway.publish(saved).subscribe())` |
| `DeleteTechnologyUseCase` | `eventGateway.publishDeleted()` | capture `existing` from `findById`, then `technologyRepository.delete(id).doOnSuccess(v -> eventGateway.publishDeleted(existing).subscribe())` |

`doOnSuccess` guarantees the event is dispatched only after a successful DB operation but without blocking the return signal. Errors from `eventGateway.publish()` must be handled inside the subscribe (logged, not propagated).

---

## Entry Point: `kafka-consumer`

**Module path:** `infrastructure/entry-points/kafka-consumer`  
**Base package:** `co.com.technology.kafka.consumer`  
**Gradle project name:** `:kafka-consumer`

### DTOs

```
dto/AuthLoginEvent.java     { String email, String token, Integer expiresIn }
dto/AuthLogoutEvent.java    { String email, String token }
```

### Mapper

```
mapper/AuthEventMapper.java   @Mapper(componentModel="spring")
  Auth toAuth(AuthLoginEvent event)
```

### Consumers

Each consumer receives messages as `ConsumerRecord<String, String>` and deserializes with `ObjectMapper` to avoid per-topic `JsonDeserializer` type configuration (messages come from external services without Spring type headers).

```java
// AuthLoginConsumer.java
@Component @RequiredArgsConstructor
@KafkaListener(topics = "auth.login.admin")
void consume(ConsumerRecord<String, String> record) →
  AuthLoginEvent event = objectMapper.readValue(record.value(), AuthLoginEvent.class)
  saveAuthService.save(mapper.toAuth(event)).subscribe()

// AuthLogoutConsumer.java
@Component @RequiredArgsConstructor
@KafkaListener(topics = "generic.auth.logout")
void consume(ConsumerRecord<String, String> record) →
  AuthLogoutEvent event = objectMapper.readValue(record.value(), AuthLogoutEvent.class)
  deleteAuthService.delete(event.getEmail(), event.getToken()).subscribe()
```

`application.yml` consumer `value-deserializer` stays `StringDeserializer` (not `JsonDeserializer`).

The `ObjectMapper` bean comes from the existing `org.reactivecommons.utils:object-mapper` dependency already in `app-service`.

### build.gradle

```groovy
dependencies {
    implementation project(':model')
    implementation project(':usecase')
    implementation 'org.springframework.kafka:spring-kafka'
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    testAnnotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

---

## Driven Adapter: `kafka-publisher`

**Module path:** `infrastructure/driven-adapters/kafka-publisher`  
**Base package:** `co.com.technology.kafka.publisher`  
**Gradle project name:** `:kafka-publisher`

### DTO

```
technology/TechnologyCatalogEvent.java   { Long id, String name }
```

### Mapper

```
technology/TechnologyEventMapper.java   @Mapper(componentModel="spring")
  TechnologyCatalogEvent toEvent(Technology technology)
```

### Adapter

```java
// TechnologyEventPublisherAdapter.java
@Component @RequiredArgsConstructor
implements TechnologyEventGateway

private static final String TOPIC_CATALOG = "sync.technologies.catalog";
private static final String TOPIC_DELETED = "sync.technologies.deleted";

// Spring Kafka 3.x: send() already returns CompletableFuture<SendResult<K,V>>
Mono<Void> publish(Technology technology):
  Mono.fromFuture(kafkaTemplate.send(TOPIC_CATALOG, mapper.toEvent(technology))).then()

Mono<Void> publishDeleted(Technology technology):
  Mono.fromFuture(kafkaTemplate.send(TOPIC_DELETED, mapper.toEvent(technology))).then()
```

### build.gradle

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework.kafka:spring-kafka'
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    testAnnotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

---

## Configuration Changes

### `settings.gradle` — add new modules

```groovy
include ':kafka-consumer'
project(':kafka-consumer').projectDir = file('./infrastructure/entry-points/kafka-consumer')
include ':kafka-publisher'
project(':kafka-publisher').projectDir = file('./infrastructure/driven-adapters/kafka-publisher')
```

### `applications/app-service/build.gradle` — wire new modules

```groovy
implementation project(':kafka-consumer')
implementation project(':kafka-publisher')
```

### `applications/app-service/src/main/resources/application.yml` — Kafka config

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: technology-ms
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      auto-offset-reset: earliest
```

---

## Files to Create / Modify

### Create (new files)

```
domain/model/.../model/technology/gateways/TechnologyEventGateway.java
domain/usecase/.../usecase/deleteauth/DeleteAuthService.java
domain/usecase/.../usecase/deleteauth/DeleteAuthUseCase.java

infrastructure/entry-points/kafka-consumer/build.gradle
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/
  dto/AuthLoginEvent.java
  dto/AuthLogoutEvent.java
  mapper/AuthEventMapper.java
  auth/AuthLoginConsumer.java
  auth/AuthLogoutConsumer.java

infrastructure/driven-adapters/kafka-publisher/build.gradle
infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/
  technology/TechnologyCatalogEvent.java
  technology/TechnologyEventMapper.java
  technology/TechnologyEventPublisherAdapter.java
```

### Modify (existing files)

```
settings.gradle                                    — add 2 new includes
applications/app-service/build.gradle             — add 2 new project deps
applications/app-service/.../application.yml      — add spring.kafka config
domain/usecase/.../createtechnology/CreateTechnologyUseCase.java  — inject + publish
domain/usecase/.../updatetechnology/UpdateTechnologyUseCase.java  — inject + publish
domain/usecase/.../deletetechnology/DeleteTechnologyUseCase.java  — inject + publish
```

---

## Verification

1. Start Kafka: `docker-compose up` (broker at `localhost:9092`)
2. Build: `./gradlew build`
3. Run app: `./gradlew :app-service:bootRun`
4. **Consumer test (auth.login.admin):** publish `{"email":"test@test.com","token":"abc","expiresIn":3600}` to `auth.login.admin` → verify record created in `auths` table
5. **Consumer test (generic.auth.logout):** publish `{"email":"test@test.com","token":"abc"}` to `generic.auth.logout` → verify record deleted from `auths` table
6. **Publisher test (create/update):** POST/PUT to `/api/v1/technologies` → verify message published to `sync.technologies.catalog`
6b. **Publisher test (delete):** DELETE to `/api/v1/technologies/{id}` → verify message published to `sync.technologies.deleted`
7. Run unit tests: `./gradlew test`
