package co.com.technology.model.technology.gateways;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TechnologyRepository {
    Mono<Technology> save(Technology technology);
    Mono<Technology> update(Technology technology);
    Mono<Technology> findById(Long id);
    Mono<Technology> findByName(String name);
    Flux<Technology> findAll(int page, int size);
    Mono<Void> delete(Long id);
    Mono<Void> deleteByIds(List<Long> ids);
    Mono<Long> count();
}
