package co.com.technology.model.technology.gateways;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyRepository {
    Mono<Technology> save(Technology technology);
    Mono<Technology> update(Technology technology);
    Mono<Technology> findById(Long id);
    Flux<Technology> findAll(int page, int size);
    Mono<Void> softDelete(Long id);
    Mono<Long> count();
}
