package co.com.technology.r2dbc.technologycapacity.repository;

import co.com.technology.r2dbc.technologycapacity.entity.TechnologyCapacityEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TechnologyCapacityEntityRepository extends R2dbcRepository<TechnologyCapacityEntity, Long> {
    Flux<TechnologyCapacityEntity> findByCapacityExternalId(Long capacityExternalId);
    Mono<Boolean> existsByTechnologyId(Long technologyId);
    Mono<Void> deleteByTechnologyId(Long technologyId);
}