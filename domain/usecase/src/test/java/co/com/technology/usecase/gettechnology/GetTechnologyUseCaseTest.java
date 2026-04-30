package co.com.technology.usecase.gettechnology;

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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTechnologyUseCaseTest {

    @Mock
    private TechnologyRepository technologyRepository;

    @InjectMocks
    private GetTechnologyUseCase useCase;

    @Test
    void getById_whenNotFound_throwsNotFoundException() {
        when(technologyRepository.findById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getById(99L))
            .expectErrorMatches(e -> e instanceof NotFoundException &&
                ((NotFoundException) e).getError() == GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)
            .verify();
    }

    @Test
    void getById_whenFound_returnsTechnology() {
        Technology tech = Technology.builder().id(1L).name("Java").description("desc").build();
        when(technologyRepository.findById(1L)).thenReturn(Mono.just(tech));

        StepVerifier.create(useCase.getById(1L))
            .expectNext(tech)
            .verifyComplete();
    }
}
