package co.com.technology.usecase.gettechnology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetTechnologyUseCase implements GetTechnologyService {

    private final TechnologyRepository technologyRepository;

    @Override
    public Mono<Technology> getById(Long id) {
        return technologyRepository.findById(id);
    }

    @Override
    public Flux<Technology> getAll(int page, int size) {
        return technologyRepository.findAll(page, size);
    }
}
