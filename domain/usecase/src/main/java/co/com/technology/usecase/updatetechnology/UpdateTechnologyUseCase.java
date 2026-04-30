package co.com.technology.usecase.updatetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

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
                .subscribe(null, error -> {}));
    }
}
