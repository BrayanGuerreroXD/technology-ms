package co.com.technology.usecase.synctechnologycapacity;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class SyncTechnologyCapacityUseCase implements SyncTechnologyCapacityService {
    private final TechnologyCapacityRepository repository;

    public Mono<Void> sync(Long capacityId, List<Long> technologyIds) {
        return repository.deleteByCapacityExternalId(capacityId)
                .then(Mono.defer(() -> {
                    if (technologyIds == null || technologyIds.isEmpty()) {
                        return Mono.empty();
                    }
                    List<TechnologyCapacity> entities = technologyIds.stream()
                            .map(techId -> TechnologyCapacity.builder()
                                    .technologyId(techId)
                                    .capacityExternalId(capacityId)
                                    .build())
                            .toList();
                    return repository.saveAll(entities).then();
                }));
    }
}
