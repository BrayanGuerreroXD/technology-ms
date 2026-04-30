package co.com.technology.usecase.gettechnology;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface GetTechnologyService {
    Mono<Technology> getById(Long id);
    Flux<Technology> getAll(int page, int size);
}
