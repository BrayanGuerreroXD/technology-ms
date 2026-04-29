package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import co.com.technology.model.technology.gateways.TechnologyEventGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TechnologyEventPublisherAdapter implements TechnologyEventGateway {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TechnologyEventMapper mapper;
    private final String topicCatalog;
    private final String topicDeleted;

    public TechnologyEventPublisherAdapter(
            KafkaTemplate<String, Object> kafkaTemplate,
            TechnologyEventMapper mapper,
            @Value("${kafka.topics.sync-technology-catalog}") String topicCatalog,
            @Value("${kafka.topics.sync-technology-deleted}") String topicDeleted) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
        this.topicCatalog = topicCatalog;
        this.topicDeleted = topicDeleted;
    }

    @Override
    public Mono<Void> publish(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(topicCatalog, mapper.toEvent(technology))).then();
    }

    @Override
    public Mono<Void> publishDeleted(Technology technology) {
        return Mono.fromFuture(kafkaTemplate.send(topicDeleted, mapper.toEvent(technology))).then();
    }
}
