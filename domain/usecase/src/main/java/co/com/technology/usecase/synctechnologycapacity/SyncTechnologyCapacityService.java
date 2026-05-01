package co.com.technology.usecase.synctechnologycapacity;

import reactor.core.publisher.Mono;

import java.util.List;

public interface SyncTechnologyCapacityService {
    Mono<Void> sync(Long capacityId, List<Long> technologyIds);
}