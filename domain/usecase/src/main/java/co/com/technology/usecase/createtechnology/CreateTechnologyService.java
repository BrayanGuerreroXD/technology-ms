package co.com.technology.usecase.createtechnology;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Mono;

public interface CreateTechnologyService {
    Mono<Technology> create(Technology technology);
}
