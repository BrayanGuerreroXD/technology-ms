package co.com.technology.r2dbc.auth.adapter;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import co.com.technology.r2dbc.auth.mapper.AuthEntityMapper;
import co.com.technology.r2dbc.auth.repository.AuthEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class AuthRepositoryAdapter implements AuthRepository {

    private final AuthEntityRepository entityRepository;
    private final AuthEntityMapper mapper;

    @Override
    public Mono<Auth> save(Auth auth) {
        return entityRepository.save(mapper.toEntity(auth))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Auth> findByEmail(String email) {
        return entityRepository.findByEmail(email)
            .map(mapper::toModel);
    }

    @Override
    public Mono<Auth> findByToken(String token) {
        return entityRepository.findByToken(token)
            .map(mapper::toModel);
    }

    @Override
    public Mono<Void> deleteByEmail(String email) {
        return entityRepository.deleteByEmail(email);
    }
}
