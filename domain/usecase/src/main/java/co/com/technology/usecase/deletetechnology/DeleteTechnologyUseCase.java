package co.com.technology.usecase.deletetechnology;

import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteTechnologyUseCase implements DeleteTechnologyService {

    private final TechnologyRepository technologyRepository;
    private final TechnologyEventGateway eventGateway;
    private final TechnologyCapacityRepository technologyCapacityRepository;

    @Override
    public Mono<Void> delete(Long id) {
        return technologyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)))
            .flatMap(existing -> technologyCapacityRepository.existsByTechnologyId(id)
                .flatMap(inUse -> {
                    if (inUse) {
                        return Mono.error(new ConflictException(GlobalExceptionEnum.TECHNOLOGY_IN_USE));
                    }
                    return technologyRepository.delete(id)
                        .doOnSuccess(v -> eventGateway.publishDeleted(existing)
                            .subscribe(null, error -> {}));
                }));
    }
}
