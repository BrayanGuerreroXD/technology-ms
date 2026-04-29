package co.com.technology.usecase.deleteauth;

import reactor.core.publisher.Mono;

public interface DeleteAuthService {
    Mono<Void> delete(String email, String token);
}
