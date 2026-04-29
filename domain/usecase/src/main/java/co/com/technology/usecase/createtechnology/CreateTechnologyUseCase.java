package co.com.technology.usecase.createtechnology;

import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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
                .subscribe(null, error -> {}));
    }
}
