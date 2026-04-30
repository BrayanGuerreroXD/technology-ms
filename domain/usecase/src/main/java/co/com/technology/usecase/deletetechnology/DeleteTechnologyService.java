package co.com.technology.usecase.deletetechnology;

import reactor.core.publisher.Mono;

public interface DeleteTechnologyService {
    Mono<Void> delete(Long id);
}
