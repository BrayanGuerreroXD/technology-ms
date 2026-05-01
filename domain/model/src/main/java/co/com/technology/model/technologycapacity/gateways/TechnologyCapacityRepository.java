package co.com.technology.model.technologycapacity.gateways;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyCapacityRepository {
    Flux<TechnologyCapacity> findByCapacityExternalId(Long capacityExternalId);
    Mono<Void> deleteByCapacityExternalId(Long capacityExternalId);
    Flux<TechnologyCapacity> saveAll(Iterable<TechnologyCapacity> entities);
    Mono<Boolean> existsByTechnologyId(Long technologyId);
}