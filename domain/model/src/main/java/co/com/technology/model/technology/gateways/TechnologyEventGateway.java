package co.com.technology.model.technology.gateways;

import co.com.technology.model.technology.Technology;
import reactor.core.publisher.Mono;

public interface TechnologyEventGateway {
    Mono<Void> publish(Technology technology);
    Mono<Void> publishDeleted(Technology technology);
}
