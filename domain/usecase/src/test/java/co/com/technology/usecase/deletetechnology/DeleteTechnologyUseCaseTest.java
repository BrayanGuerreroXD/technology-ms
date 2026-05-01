package co.com.technology.usecase.deletetechnology;

import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
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

    @Mock
    private TechnologyCapacityRepository technologyCapacityRepository;

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
    void delete_whenInUse_throwsConflictException() {
        Technology existing = Technology.builder().id(1L).name("Java").description("desc").build();
        when(technologyRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(technologyCapacityRepository.existsByTechnologyId(1L)).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.delete(1L))
            .expectErrorMatches(e -> e instanceof ConflictException &&
                ((ConflictException) e).getError() == GlobalExceptionEnum.TECHNOLOGY_IN_USE)
            .verify();
    }

    @Test
    void delete_whenFoundAndNotInUse_deletesAndPublishesDeletedEvent() {
        Technology existing = Technology.builder().id(1L).name("Java").description("desc").build();
        when(technologyRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(technologyCapacityRepository.existsByTechnologyId(1L)).thenReturn(Mono.just(false));
        when(technologyRepository.delete(1L)).thenReturn(Mono.empty());
        when(eventGateway.publishDeleted(existing)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete(1L))
            .verifyComplete();

        verify(technologyRepository).delete(1L);
        verify(eventGateway).publishDeleted(existing);
    }
}