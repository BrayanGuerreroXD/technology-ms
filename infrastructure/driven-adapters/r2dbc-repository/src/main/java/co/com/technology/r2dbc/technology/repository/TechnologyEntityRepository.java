package co.com.technology.r2dbc.technology.repository;

import co.com.technology.r2dbc.technology.entity.TechnologyEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyEntityRepository extends ReactiveCrudRepository<TechnologyEntity, Long> {
    Flux<TechnologyEntity> findAllBy(Pageable pageable);
    Mono<TechnologyEntity> findByName(String name);
}
