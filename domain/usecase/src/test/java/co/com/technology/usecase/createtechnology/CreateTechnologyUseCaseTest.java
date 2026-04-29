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
