package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TechnologyEventPublisherAdapter implements TechnologyEventGateway {

    private static final String TOPIC_CATALOG = "sync.technologies.catalog";
    private static final String TOPIC_DELETED = "sync.technologies.deleted";

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TechnologyEventMapper mapper;

    @Override
    public Mono<Void> publish(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(TOPIC_CATALOG, mapper.toEvent(technology))).then();
    }

    @Override
    public Mono<Void> publishDeleted(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(TOPIC_DELETED, mapper.toEvent(technology))).then();
    }
}
