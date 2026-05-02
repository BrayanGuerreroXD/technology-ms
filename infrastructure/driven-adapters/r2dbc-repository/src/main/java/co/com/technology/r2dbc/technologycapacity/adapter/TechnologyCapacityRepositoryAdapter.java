package co.com.technology.r2dbc.technologycapacity.adapter;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import co.com.technology.r2dbc.technologycapacity.mapper.TechnologyCapacityEntityMapper;
import co.com.technology.r2dbc.technologycapacity.repository.TechnologyCapacityEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TechnologyCapacityRepositoryAdapter implements TechnologyCapacityRepository {

    private final TechnologyCapacityEntityRepository entityRepository;
    private final TechnologyCapacityEntityMapper mapper;

    @Override
    public Flux<TechnologyCapacity> findByCapacityExternalId(Long capacityExternalId) {
        return entityRepository.findByCapacityExternalId(capacityExternalId)
                .map(mapper::toModel);
    }

    @Override
    public Mono<Void> deleteByCapacityExternalId(Long capacityExternalId) {
        return entityRepository.findByCapacityExternalId(capacityExternalId)
                .flatMap(entity -> entityRepository.deleteById(entity.getId()))
                .then();
    }

    @Override
    public Flux<TechnologyCapacity> saveAll(Iterable<TechnologyCapacity> entities) {
        return entityRepository.saveAll(Flux.fromIterable(entities).map(mapper::toEntity))
                .map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByTechnologyId(Long technologyId) {
        return entityRepository.existsByTechnologyId(technologyId);
    }

    @Override
    public Mono<Void> deleteByTechnologyId(Long technologyId) {
        return entityRepository.deleteByTechnologyId(technologyId);
    }
}