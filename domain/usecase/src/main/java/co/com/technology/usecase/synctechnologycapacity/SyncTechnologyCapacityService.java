package co.com.technology.usecase.synctechnologycapacity;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SyncTechnologyCapacityService {

    private final TechnologyCapacityRepository repository;

    public Mono<Void> sync(Long capacityId, List<Long> technologyIds) {
        return repository.findByCapacityExternalId(capacityId)
                .flatMap(existing -> repository.deleteByCapacityExternalId(capacityId))
                .thenMany(Flux.fromIterable(technologyIds))
                .map(techId -> TechnologyCapacity.builder()
                        .technologyId(techId)
                        .capacityExternalId(capacityId)
                        .build())
                .as(entities -> repository.saveAll(entities))
                .then();
    }
}