package co.com.technology.model.auth.gateways;

import co.com.technology.model.auth.Auth;
import reactor.core.publisher.Mono;

public interface AuthRepository {
    Mono<Auth> save(Auth auth);
    Mono<Auth> findByEmail(String email);
    Mono<Auth> findByToken(String token);
    Mono<Void> deleteByEmail(String email);
}
