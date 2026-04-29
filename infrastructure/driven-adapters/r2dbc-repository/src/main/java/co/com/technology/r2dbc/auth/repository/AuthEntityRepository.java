package co.com.technology.r2dbc.auth.repository;

import co.com.technology.r2dbc.auth.entity.AuthEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AuthEntityRepository extends ReactiveCrudRepository<AuthEntity, Long> {
    Mono<AuthEntity> findByEmail(String email);
    Mono<AuthEntity> findByToken(String token);
    Mono<Void> deleteByEmail(String email);
}
