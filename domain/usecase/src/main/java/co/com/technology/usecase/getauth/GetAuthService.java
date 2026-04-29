package co.com.technology.usecase.getauth;

import co.com.technology.model.security.LoggedUser;
import reactor.core.publisher.Mono;

public interface GetAuthService {
    Mono<LoggedUser> getByToken(String token);
}
