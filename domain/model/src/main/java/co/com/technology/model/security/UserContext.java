package co.com.technology.model.security;

import reactor.core.publisher.Mono;

public interface UserContext {
    Mono<LoggedUser> currentUser();
}
