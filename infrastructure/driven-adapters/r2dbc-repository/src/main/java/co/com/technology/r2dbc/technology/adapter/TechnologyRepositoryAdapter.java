package co.com.technology.r2dbc.technology.adapter;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyRepository;
import co.com.technology.r2dbc.technology.mapper.TechnologyEntityMapper;
import co.com.technology.r2dbc.technology.repository.TechnologyEntityRepository;
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
        return entityRepository.findById(id)
                .map(mapper::toModel);
    }

    @Override
    public Mono<Technology> findByName(String name) {
        return entityRepository.findByName(name)
                .map(mapper::toModel);
    }

    @Override
    public Flux<Technology> findAll(int page, int size) {
        return entityRepository.findAllBy(PageRequest.of(page, size))
                .map(mapper::toModel);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return entityRepository.deleteById(id);
    }

    @Override
    public Mono<Long> count() {
        return entityRepository.count();
    }
}
