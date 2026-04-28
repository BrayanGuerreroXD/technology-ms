package co.com.technology.usecase.saveauth;

import co.com.technology.model.auth.Auth;
import reactor.core.publisher.Mono;

public interface SaveAuthService {
    Mono<Auth> save(Auth auth);
}
