package co.com.technology.r2dbc.technology;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TechnologyEntityRepository extends ReactiveCrudRepository<TechnologyEntity, Long> {
    Flux<TechnologyEntity> findAllByDeletedAtIsNull(Pageable pageable);
    Mono<TechnologyEntity> findByIdAndDeletedAtIsNull(Long id);
    Mono<Long> countByDeletedAtIsNull();

    @Modifying
    @Query("UPDATE technology SET deleted_at = NOW() WHERE id = :id")
    Mono<Integer> softDeleteById(Long id);
}
