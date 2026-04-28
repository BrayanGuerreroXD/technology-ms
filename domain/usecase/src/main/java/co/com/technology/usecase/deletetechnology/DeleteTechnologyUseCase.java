package co.com.technology.usecase.deletetechnology;

import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteTechnologyUseCase implements DeleteTechnologyService {

    private final TechnologyRepository technologyRepository;

    @Override
    public Mono<Void> delete(Long id) {
        return technologyRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)))
            .flatMap(existing -> technologyRepository.delete(id));
    }
}
