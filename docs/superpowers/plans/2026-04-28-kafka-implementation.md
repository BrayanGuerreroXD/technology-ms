# Kafka Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate spring-kafka into technology-ms as both consumer (auth.login.admin, generic.auth.logout) and publisher (sync.technologies.catalog, sync.technologies.deleted), following hexagonal architecture with two new Gradle modules.

**Architecture:** Kafka consumers live in a new `kafka-consumer` entry-point module that calls existing use cases. A new `kafka-publisher` driven-adapter module implements `TechnologyEventGateway`, which the three Technology use cases call as a fire-and-forget side effect via `doOnSuccess`. A new `DeleteAuthUseCase` handles logout events.

**Tech Stack:** Spring Boot 4.0.5, Spring WebFlux, spring-kafka, MapStruct 1.6.3, Lombok, Mockito + StepVerifier for tests.

---

## File Map

### Create
```
domain/model/src/main/java/co/com/technology/model/technology/gateways/TechnologyEventGateway.java
domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/DeleteAuthService.java
domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/DeleteAuthUseCase.java
domain/usecase/src/test/java/co/com/technology/usecase/deleteauth/DeleteAuthUseCaseTest.java

infrastructure/driven-adapters/kafka-publisher/build.gradle
infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyCatalogEvent.java
infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyEventMapper.java
infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyEventPublisherAdapter.java
infrastructure/driven-adapters/kafka-publisher/src/test/java/co/com/technology/kafka/publisher/technology/TechnologyEventPublisherAdapterTest.java

infrastructure/entry-points/kafka-consumer/build.gradle
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/dto/AuthLoginEvent.java
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/dto/AuthLogoutEvent.java
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/mapper/AuthEventMapper.java
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/auth/AuthLoginConsumer.java
infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/auth/AuthLogoutConsumer.java
infrastructure/entry-points/kafka-consumer/src/test/java/co/com/technology/kafka/consumer/auth/AuthLoginConsumerTest.java
infrastructure/entry-points/kafka-consumer/src/test/java/co/com/technology/kafka/consumer/auth/AuthLogoutConsumerTest.java
```

### Modify
```
settings.gradle                                                                   — +2 module includes
applications/app-service/build.gradle                                             — +2 project deps
applications/app-service/src/main/resources/application.yml                      — +spring.kafka config
domain/usecase/src/main/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCase.java
domain/usecase/src/main/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCase.java
domain/usecase/src/main/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCase.java
domain/usecase/src/test/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCaseTest.java
domain/usecase/src/test/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCaseTest.java
domain/usecase/src/test/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCaseTest.java
```

---

## Task 1: TechnologyEventGateway interface

**Files:**
- Create: `domain/model/src/main/java/co/com/technology/model/technology/gateways/TechnologyEventGateway.java`

- [ ] **Step 1: Create the gateway interface**

```java
package co.com.technology.model.technology.gateways;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Mono;

public interface TechnologyEventGateway {
    Mono<Void> publish(Technology technology);
    Mono<Void> publishDeleted(Technology technology);
}
```

- [ ] **Step 2: Compile domain/model**

```bash
./gradlew :model:compileJava
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add domain/model/src/main/java/co/com/technology/model/technology/gateways/TechnologyEventGateway.java
git commit -m "feat(domain): add TechnologyEventGateway interface for Kafka publishing"
```

---

## Task 2: DeleteAuthUseCase

**Files:**
- Create: `domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/DeleteAuthService.java`
- Create: `domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/DeleteAuthUseCase.java`
- Create: `domain/usecase/src/test/java/co/com/technology/usecase/deleteauth/DeleteAuthUseCaseTest.java`

- [ ] **Step 1: Write failing test**

```java
package co.com.technology.usecase.deleteauth;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAuthUseCaseTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private DeleteAuthUseCase useCase;

    @Test
    void delete_whenEmailFoundAndTokenMatches_shouldDeleteByEmail() {
        Auth auth = Auth.builder().email("user@test.com").token("tok123").build();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.just(auth));
        when(authRepository.deleteByEmail("user@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("user@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository).deleteByEmail("user@test.com");
    }

    @Test
    void delete_whenEmailFoundButTokenDoesNotMatch_shouldDoNothing() {
        Auth auth = Auth.builder().email("user@test.com").token("different").build();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.just(auth));

        StepVerifier.create(useCase.delete("user@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository, never()).deleteByEmail(any());
    }

    @Test
    void delete_whenEmailNotFound_shouldDoNothing() {
        when(authRepository.findByEmail("noone@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("noone@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository, never()).deleteByEmail(any());
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.deleteauth.DeleteAuthUseCaseTest"
```

Expected: FAIL — `DeleteAuthUseCase` class not found.

- [ ] **Step 3: Create DeleteAuthService interface**

```java
package co.com.technology.usecase.deleteauth;

import reactor.core.publisher.Mono;

public interface DeleteAuthService {
    Mono<Void> delete(String email, String token);
}
```

- [ ] **Step 4: Create DeleteAuthUseCase implementation**

```java
package co.com.technology.usecase.deleteauth;

import co.com.technology.model.auth.gateways.AuthRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteAuthUseCase implements DeleteAuthService {

    private final AuthRepository authRepository;

    @Override
    public Mono<Void> delete(String email, String token) {
        return authRepository.findByEmail(email)
            .filter(auth -> auth.getToken().equals(token))
            .flatMap(auth -> authRepository.deleteByEmail(auth.getEmail()));
    }
}
```

- [ ] **Step 5: Run tests to verify they pass**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.deleteauth.DeleteAuthUseCaseTest"
```

Expected: BUILD SUCCESSFUL — 3 tests passed.

- [ ] **Step 6: Commit**

```bash
git add domain/usecase/src/main/java/co/com/technology/usecase/deleteauth/ \
        domain/usecase/src/test/java/co/com/technology/usecase/deleteauth/
git commit -m "feat(usecase): add DeleteAuthUseCase for logout event processing"
```

---

## Task 3: Update CreateTechnologyUseCase — fire-and-forget publish

**Files:**
- Modify: `domain/usecase/src/main/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCase.java`
- Modify: `domain/usecase/src/test/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCaseTest.java`

- [ ] **Step 1: Update the test to add TechnologyEventGateway mock**

Replace the entire test file content:

```java
package co.com.technology.usecase.createtechnology;

import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private TechnologyEventGateway eventGateway;

    @InjectMocks
    private CreateTechnologyUseCase useCase;

    @Test
    void create_whenNameAlreadyExists_throwsConflictException() {
        Technology existing = Technology.builder().id(1L).name("Java").description("desc").build();
        Technology input = Technology.builder().name("Java").description("new desc").build();

        when(technologyRepository.findByName("Java")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.create(input))
            .expectErrorMatches(e -> e instanceof ConflictException &&
                ((ConflictException) e).getError() == GlobalExceptionEnum.TECHNOLOGY_NAME_ALREADY_EXISTS)
            .verify();
    }

    @Test
    void create_whenNameIsUnique_savesAndPublishesEvent() {
        Technology input = Technology.builder().name("Kotlin").description("JVM language").build();
        Technology saved = Technology.builder().id(2L).name("Kotlin").description("JVM language").build();

        when(technologyRepository.findByName("Kotlin")).thenReturn(Mono.empty());
        when(technologyRepository.save(any())).thenReturn(Mono.just(saved));
        when(eventGateway.publish(saved)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.create(input))
            .expectNext(saved)
            .verifyComplete();

        verify(eventGateway).publish(saved);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.createtechnology.CreateTechnologyUseCaseTest"
```

Expected: FAIL — `CreateTechnologyUseCase` constructor mismatch (no `TechnologyEventGateway` field yet).

- [ ] **Step 3: Update CreateTechnologyUseCase implementation**

```java
package co.com.technology.usecase.createtechnology;

import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CreateTechnologyUseCase implements CreateTechnologyService {

    private final TechnologyRepository technologyRepository;
    private final TechnologyEventGateway eventGateway;

    @Override
    public Mono<Technology> create(Technology technology) {
        return technologyRepository.findByName(technology.getName())
            .flatMap(existing -> Mono.<Technology>error(
                new ConflictException(GlobalExceptionEnum.TECHNOLOGY_NAME_ALREADY_EXISTS)))
            .switchIfEmpty(Mono.defer(() -> technologyRepository.save(technology)))
            .doOnSuccess(saved -> eventGateway.publish(saved)
                .subscribe(null, error -> log.error("Error publishing create event: {}", error.getMessage())));
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.createtechnology.CreateTechnologyUseCaseTest"
```

Expected: BUILD SUCCESSFUL — 2 tests passed.

- [ ] **Step 5: Commit**

```bash
git add domain/usecase/src/main/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCase.java \
        domain/usecase/src/test/java/co/com/technology/usecase/createtechnology/CreateTechnologyUseCaseTest.java
git commit -m "feat(usecase): publish sync.technologies.catalog event on technology create"
```

---

## Task 4: Update UpdateTechnologyUseCase — fire-and-forget publish

**Files:**
- Modify: `domain/usecase/src/main/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCase.java`
- Modify: `domain/usecase/src/test/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCaseTest.java`

- [ ] **Step 1: Update the test to add TechnologyEventGateway mock**

Replace the entire test file content:

```java
package co.com.technology.usecase.updatetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private TechnologyEventGateway eventGateway;

    @InjectMocks
    private UpdateTechnologyUseCase useCase;

    @Test
    void update_whenNotFound_throwsNotFoundException() {
        when(technologyRepository.findById(99L)).thenReturn(Mono.empty());
        Technology input = Technology.builder().name("Go").description("systems lang").build();

        StepVerifier.create(useCase.update(99L, input))
            .expectErrorMatches(e -> e instanceof NotFoundException &&
                ((NotFoundException) e).getError() == GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)
            .verify();
    }

    @Test
    void update_whenFound_updatesAndPublishesEvent() {
        Technology existing = Technology.builder().id(1L).name("Java").description("old").build();
        Technology input = Technology.builder().name("Java").description("updated").build();
        Technology updated = Technology.builder().id(1L).name("Java").description("updated").build();

        when(technologyRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(technologyRepository.update(any())).thenReturn(Mono.just(updated));
        when(eventGateway.publish(updated)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.update(1L, input))
            .expectNext(updated)
            .verifyComplete();

        verify(eventGateway).publish(updated);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.updatetechnology.UpdateTechnologyUseCaseTest"
```

Expected: FAIL — constructor mismatch.

- [ ] **Step 3: Update UpdateTechnologyUseCase implementation**

```java
package co.com.technology.usecase.updatetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class UpdateTechnologyUseCase implements UpdateTechnologyService {

    private final TechnologyRepository technologyRepository;
    private final TechnologyEventGateway eventGateway;

    @Override
    public Mono<Technology> update(Long id, Technology technology) {
        return technologyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)))
            .flatMap(existing -> technologyRepository.update(
                technology.toBuilder().id(id).build()
            ))
            .doOnSuccess(saved -> eventGateway.publish(saved)
                .subscribe(null, error -> log.error("Error publishing update event: {}", error.getMessage())));
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.updatetechnology.UpdateTechnologyUseCaseTest"
```

Expected: BUILD SUCCESSFUL — 2 tests passed.

- [ ] **Step 5: Commit**

```bash
git add domain/usecase/src/main/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCase.java \
        domain/usecase/src/test/java/co/com/technology/usecase/updatetechnology/UpdateTechnologyUseCaseTest.java
git commit -m "feat(usecase): publish sync.technologies.catalog event on technology update"
```

---

## Task 5: Update DeleteTechnologyUseCase — fire-and-forget publishDeleted

**Files:**
- Modify: `domain/usecase/src/main/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCase.java`
- Modify: `domain/usecase/src/test/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCaseTest.java`

- [ ] **Step 1: Update the test to add TechnologyEventGateway mock**

Replace the entire test file content:

```java
package co.com.technology.usecase.deletetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @Mock
    private TechnologyEventGateway eventGateway;

    @InjectMocks
    private DeleteTechnologyUseCase useCase;

    @Test
    void delete_whenNotFound_throwsNotFoundException() {
        when(technologyRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete(99L))
            .expectErrorMatches(e -> e instanceof NotFoundException &&
                ((NotFoundException) e).getError() == GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)
            .verify();
    }

    @Test
    void delete_whenFound_deletesAndPublishesDeletedEvent() {
        Technology existing = Technology.builder().id(1L).name("Java").description("desc").build();
        when(technologyRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(technologyRepository.delete(1L)).thenReturn(Mono.empty());
        when(eventGateway.publishDeleted(existing)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete(1L))
            .verifyComplete();

        verify(eventGateway).publishDeleted(existing);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
./gradlew :usecase:test --tests "co.com.technology.usecase.deletetechnology.DeleteTechnologyUseCaseTest"
```

Expected: FAIL — constructor mismatch.

- [ ] **Step 3: Update DeleteTechnologyUseCase implementation**

```java
package co.com.technology.usecase.deletetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class DeleteTechnologyUseCase implements DeleteTechnologyService {

    private final TechnologyRepository technologyRepository;
    private final TechnologyEventGateway eventGateway;

    @Override
    public Mono<Void> delete(Long id) {
        return technologyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)))
            .flatMap(existing -> technologyRepository.delete(id)
                .doOnSuccess(v -> eventGateway.publishDeleted(existing)
                    .subscribe(null, error -> log.error("Error publishing delete event: {}", error.getMessage()))));
    }
}
```

- [ ] **Step 4: Run all usecase tests**

```bash
./gradlew :usecase:test
```

Expected: BUILD SUCCESSFUL — all tests pass.

- [ ] **Step 5: Commit**

```bash
git add domain/usecase/src/main/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCase.java \
        domain/usecase/src/test/java/co/com/technology/usecase/deletetechnology/DeleteTechnologyUseCaseTest.java
git commit -m "feat(usecase): publish sync.technologies.deleted event on technology delete"
```

---

## Task 6: kafka-publisher driven-adapter module

**Files:**
- Create: `infrastructure/driven-adapters/kafka-publisher/build.gradle`
- Create: `infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyCatalogEvent.java`
- Create: `infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyEventMapper.java`
- Create: `infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology/TechnologyEventPublisherAdapter.java`
- Create: `infrastructure/driven-adapters/kafka-publisher/src/test/java/co/com/technology/kafka/publisher/technology/TechnologyEventPublisherAdapterTest.java`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infrastructure/driven-adapters/kafka-publisher/src/main/java/co/com/technology/kafka/publisher/technology
mkdir -p infrastructure/driven-adapters/kafka-publisher/src/test/java/co/com/technology/kafka/publisher/technology
```

- [ ] **Step 2: Create build.gradle**

```groovy
dependencies {
    implementation project(':model')
    implementation 'org.springframework.kafka:spring-kafka'
    implementation "org.mapstruct:mapstruct:${mapstructVersion}"
    annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
    testAnnotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
}
```

- [ ] **Step 3: Create TechnologyCatalogEvent DTO**

```java
package co.com.technology.kafka.publisher.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnologyCatalogEvent {
    private Long id;
    private String name;
}
```

- [ ] **Step 4: Create TechnologyEventMapper**

```java
package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyEventMapper {
    TechnologyCatalogEvent toEvent(Technology technology);
}
```

- [ ] **Step 5: Write failing test for adapter**

```java
package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private TechnologyEventMapper mapper;

    @InjectMocks
    private TechnologyEventPublisherAdapter adapter;

    private SendResult<String, Object> mockSendResult() {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("topic", "value");
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("topic", 0), 0, 0, 0, 0, 0);
        return new SendResult<>(producerRecord, metadata);
    }

    @Test
    void publish_sendsToSyncTechnologiesCatalogTopic() {
        Technology tech = Technology.builder().id(1L).name("Java").description("x").build();
        TechnologyCatalogEvent event = new TechnologyCatalogEvent(1L, "Java");
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockSendResult());

        when(mapper.toEvent(tech)).thenReturn(event);
        when(kafkaTemplate.send("sync.technologies.catalog", event)).thenReturn(future);

        StepVerifier.create(adapter.publish(tech))
            .verifyComplete();
    }

    @Test
    void publishDeleted_sendsToSyncTechnologiesDeletedTopic() {
        Technology tech = Technology.builder().id(2L).name("Go").description("x").build();
        TechnologyCatalogEvent event = new TechnologyCatalogEvent(2L, "Go");
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockSendResult());

        when(mapper.toEvent(tech)).thenReturn(event);
        when(kafkaTemplate.send("sync.technologies.deleted", event)).thenReturn(future);

        StepVerifier.create(adapter.publishDeleted(tech))
            .verifyComplete();
    }
}
```

- [ ] **Step 6: Create TechnologyEventPublisherAdapter**

```java
package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TechnologyEventPublisherAdapter implements TechnologyEventGateway {

    private static final String TOPIC_CATALOG = "sync.technologies.catalog";
    private static final String TOPIC_DELETED = "sync.technologies.deleted";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TechnologyEventMapper mapper;

    @Override
    public Mono<Void> publish(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(TOPIC_CATALOG, mapper.toEvent(technology))).then();
    }

    @Override
    public Mono<Void> publishDeleted(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(TOPIC_DELETED, mapper.toEvent(technology))).then();
    }
}
```

- [ ] **Step 7: Wire module in settings.gradle and app-service/build.gradle**

In `settings.gradle`, add after the last `include` block:
```groovy
include ':kafka-publisher'
project(':kafka-publisher').projectDir = file('./infrastructure/driven-adapters/kafka-publisher')
```

In `applications/app-service/build.gradle`, add:
```groovy
implementation project(':kafka-publisher')
```

- [ ] **Step 8: Compile and run publisher tests**

```bash
./gradlew :kafka-publisher:test
```

Expected: BUILD SUCCESSFUL — 2 tests passed.

- [ ] **Step 9: Commit**

```bash
git add infrastructure/driven-adapters/kafka-publisher/ \
        settings.gradle \
        applications/app-service/build.gradle
git commit -m "feat(infra): add kafka-publisher driven-adapter with TechnologyEventPublisherAdapter"
```

---

## Task 7: kafka-consumer entry-point module

**Files:**
- Create: `infrastructure/entry-points/kafka-consumer/build.gradle`
- Create: `infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/dto/AuthLoginEvent.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/dto/AuthLogoutEvent.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/mapper/AuthEventMapper.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/auth/AuthLoginConsumer.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/auth/AuthLogoutConsumer.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/test/java/co/com/technology/kafka/consumer/auth/AuthLoginConsumerTest.java`
- Create: `infrastructure/entry-points/kafka-consumer/src/test/java/co/com/technology/kafka/consumer/auth/AuthLogoutConsumerTest.java`

- [ ] **Step 1: Create directory structure**

```bash
mkdir -p infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/dto
mkdir -p infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/mapper
mkdir -p infrastructure/entry-points/kafka-consumer/src/main/java/co/com/technology/kafka/consumer/auth
mkdir -p infrastructure/entry-points/kafka-consumer/src/test/java/co/com/technology/kafka/consumer/auth
```

- [ ] **Step 2: Create build.gradle**

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

- [ ] **Step 3: Create AuthLoginEvent DTO**

```java
package co.com.technology.kafka.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginEvent {
    private String email;
    private String token;
    private Integer expiresIn;
}
```

- [ ] **Step 4: Create AuthLogoutEvent DTO**

```java
package co.com.technology.kafka.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogoutEvent {
    private String email;
    private String token;
}
```

- [ ] **Step 5: Create AuthEventMapper**

```java
package co.com.technology.kafka.consumer.mapper;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.model.auth.Auth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthEventMapper {
    Auth toAuth(AuthLoginEvent event);
}
```

- [ ] **Step 6: Write failing tests for consumers**

```java
// AuthLoginConsumerTest.java
package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.kafka.consumer.mapper.AuthEventMapper;
import co.com.technology.model.auth.Auth;
import co.com.technology.usecase.saveauth.SaveAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthLoginConsumerTest {

    @Mock
    private SaveAuthService saveAuthService;

    @Mock
    private AuthEventMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthLoginConsumer consumer;

    @Test
    void consume_parsesEventAndCallsSave() throws Exception {
        String json = "{\"email\":\"u@t.com\",\"token\":\"tok\",\"expiresIn\":3600}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("auth.login.admin", 0, 0, null, json);
        AuthLoginEvent event = new AuthLoginEvent("u@t.com", "tok", 3600);
        Auth auth = Auth.builder().email("u@t.com").token("tok").expiresIn(3600).build();

        when(objectMapper.readValue(json, AuthLoginEvent.class)).thenReturn(event);
        when(mapper.toAuth(event)).thenReturn(auth);
        when(saveAuthService.save(auth)).thenReturn(Mono.just(auth));

        consumer.consume(record);

        verify(saveAuthService).save(auth);
    }
}
```

```java
// AuthLogoutConsumerTest.java
package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLogoutEvent;
import co.com.technology.usecase.deleteauth.DeleteAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthLogoutConsumerTest {

    @Mock
    private DeleteAuthService deleteAuthService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthLogoutConsumer consumer;

    @Test
    void consume_parsesEventAndCallsDelete() throws Exception {
        String json = "{\"email\":\"u@t.com\",\"token\":\"tok\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("generic.auth.logout", 0, 0, null, json);
        AuthLogoutEvent event = new AuthLogoutEvent("u@t.com", "tok");

        when(objectMapper.readValue(json, AuthLogoutEvent.class)).thenReturn(event);
        when(deleteAuthService.delete("u@t.com", "tok")).thenReturn(Mono.empty());

        consumer.consume(record);

        verify(deleteAuthService).delete("u@t.com", "tok");
    }
}
```

- [ ] **Step 7: Create AuthLoginConsumer**

```java
package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.kafka.consumer.mapper.AuthEventMapper;
import co.com.technology.usecase.saveauth.SaveAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLoginConsumer {

    private final SaveAuthService saveAuthService;
    private final AuthEventMapper mapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "auth.login.admin")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            AuthLoginEvent event = objectMapper.readValue(record.value(), AuthLoginEvent.class);
            saveAuthService.save(mapper.toAuth(event))
                .subscribe(null, error -> log.error("Error saving auth login for {}: {}", event.getEmail(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing auth.login.admin message: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 8: Create AuthLogoutConsumer**

```java
package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLogoutEvent;
import co.com.technology.usecase.deleteauth.DeleteAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLogoutConsumer {

    private final DeleteAuthService deleteAuthService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "generic.auth.logout")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            AuthLogoutEvent event = objectMapper.readValue(record.value(), AuthLogoutEvent.class);
            deleteAuthService.delete(event.getEmail(), event.getToken())
                .subscribe(null, error -> log.error("Error processing logout for {}: {}", event.getEmail(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing generic.auth.logout message: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 9: Wire module in settings.gradle and app-service/build.gradle**

In `settings.gradle`, add:
```groovy
include ':kafka-consumer'
project(':kafka-consumer').projectDir = file('./infrastructure/entry-points/kafka-consumer')
```

In `applications/app-service/build.gradle`, add:
```groovy
implementation project(':kafka-consumer')
```

- [ ] **Step 10: Run consumer tests**

```bash
./gradlew :kafka-consumer:test
```

Expected: BUILD SUCCESSFUL — 2 tests passed.

- [ ] **Step 11: Commit**

```bash
git add infrastructure/entry-points/kafka-consumer/ \
        settings.gradle \
        applications/app-service/build.gradle
git commit -m "feat(infra): add kafka-consumer entry-point with auth login/logout listeners"
```

---

## Task 8: Kafka configuration in application.yml and full build verification

**Files:**
- Modify: `applications/app-service/src/main/resources/application.yml`

- [ ] **Step 1: Add spring.kafka config to application.yml**

Add the following block at the end of `application.yml`:

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

Note: `application.yml` already has a `spring:` key. Merge the `kafka:` block under the existing `spring:` key — do not create a duplicate `spring:` root.

- [ ] **Step 2: Run full build**

```bash
./gradlew build -x pitest
```

Expected: BUILD SUCCESSFUL — all modules compile, all tests pass.

- [ ] **Step 3: Commit**

```bash
git add applications/app-service/src/main/resources/application.yml
git commit -m "feat(config): add Kafka bootstrap-servers and producer/consumer configuration"
```

---

## Verification

1. Start Kafka: `docker-compose up -d`
2. Start app: `./gradlew :app-service:bootRun`
3. **Consumer (auth.login.admin):** publish `{"email":"test@test.com","token":"abc123","expiresIn":3600}` to topic `auth.login.admin` → verify row created in `auths` table
4. **Consumer (generic.auth.logout):** publish `{"email":"test@test.com","token":"abc123"}` to topic `generic.auth.logout` → verify row deleted from `auths` table
5. **Publisher (create):** `POST /api/v1/technologies` with `{"name":"Rust","description":"Systems language"}` → verify message in `sync.technologies.catalog`
6. **Publisher (update):** `PUT /api/v1/technologies/{id}` → verify message in `sync.technologies.catalog`
7. **Publisher (delete):** `DELETE /api/v1/technologies/{id}` → verify message in `sync.technologies.deleted`
