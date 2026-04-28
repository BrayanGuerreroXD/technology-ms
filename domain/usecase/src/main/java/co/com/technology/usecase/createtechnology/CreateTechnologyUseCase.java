package co.com.technology.usecase.createtechnology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateTechnologyUseCase implements CreateTechnologyService {

    private final TechnologyRepository technologyRepository;

    @Override
    public Mono<Technology> create(Technology technology) {
        return technologyRepository.save(technology);
    }
}
