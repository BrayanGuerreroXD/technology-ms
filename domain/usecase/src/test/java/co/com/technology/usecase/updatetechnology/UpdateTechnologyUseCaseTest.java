package co.com.technology.usecase.updatetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

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
    void update_whenFound_updatesAndReturnsTechnology() {
        Technology existing = Technology.builder().id(1L).name("Java").description("old").build();
        Technology input = Technology.builder().name("Java").description("updated").build();
        Technology updated = Technology.builder().id(1L).name("Java").description("updated").build();

        when(technologyRepository.findById(1L)).thenReturn(Mono.just(existing));
        when(technologyRepository.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.update(1L, input))
            .expectNext(updated)
            .verifyComplete();
    }
}
