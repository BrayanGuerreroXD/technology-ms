package co.com.technology.usecase.deletetechnologymatch;

import co.com.technology.model.technology.gateways.TechnologyRepository;
import co.com.technology.model.technologycapacity.gateways.TechnologyCapacityRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class DeleteTechnologyMatchUseCase implements DeleteTechnologyMatchService {

    private final TechnologyRepository technologyRepository;
    private final TechnologyCapacityRepository technologyCapacityRepository;

    @Override
    public Mono<Void> delete(List<Long> technologyIds) {
        return Mono.when(
            technologyIds.stream()
                .map(id -> technologyCapacityRepository.deleteByTechnologyId(id)
                    .then(technologyRepository.delete(id)))
                .toArray(Mono[]::new)
        );
    }
}