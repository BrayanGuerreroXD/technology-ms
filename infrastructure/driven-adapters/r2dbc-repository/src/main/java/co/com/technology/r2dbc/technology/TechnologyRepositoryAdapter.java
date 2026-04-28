package co.com.technology.r2dbc.technology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class TechnologyRepositoryAdapter implements TechnologyRepository {

    private final TechnologyEntityRepository entityRepository;
    private final TechnologyEntityMapper mapper;

    @Override
    public Mono<Technology> save(Technology technology) {
        return entityRepository.save(mapper.toEntity(technology))
                .map(mapper::toModel);
    }

    @Override
    public Mono<Technology> update(Technology technology) {
        return entityRepository.save(mapper.toEntity(technology))
                .map(mapper::toModel);
    }

    @Override
    public Mono<Technology> findById(Long id) {
        return entityRepository.findByIdAndDeletedAtIsNull(id)
                .map(mapper::toModel);
    }

    @Override
    public Flux<Technology> findAll(int page, int size) {
        return entityRepository.findAllByDeletedAtIsNull(PageRequest.of(page, size))
                .map(mapper::toModel);
    }

    @Override
    public Mono<Void> softDelete(Long id) {
        return entityRepository.softDeleteById(id).then();
    }

    @Override
    public Mono<Long> count() {
        return entityRepository.countByDeletedAtIsNull();
    }
}
