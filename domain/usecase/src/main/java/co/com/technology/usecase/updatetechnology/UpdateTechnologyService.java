package co.com.technology.usecase.updatetechnology;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Mono;

public interface UpdateTechnologyService {
    Mono<Technology> update(Long id, Technology technology);
}
