package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import reactor.test.StepVerifier;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyEventPublisherAdapterTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private TechnologyEventMapper mapper;

    @InjectMocks
    private TechnologyEventPublisherAdapter adapter;

    private SendResult<String, Object> mockSendResult() {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("topic", "value");
        RecordMetadata metadata = new RecordMetadata(new TopicPartition("topic", 0), 0, 0, 0, 0, 0);
        return new SendResult<>(producerRecord, metadata);
    }

    @Test
    void publish_sendsToSyncTechnologiesCatalogTopic() {
        Technology tech = Technology.builder().id(1L).name("Java").description("x").build();
        TechnologyCatalogEvent event = new TechnologyCatalogEvent(1L, "Java");
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockSendResult());

        when(mapper.toEvent(tech)).thenReturn(event);
        when(kafkaTemplate.send("sync.technologies.catalog", event)).thenReturn(future);

        StepVerifier.create(adapter.publish(tech))
            .verifyComplete();
    }

    @Test
    void publishDeleted_sendsToSyncTechnologiesDeletedTopic() {
        Technology tech = Technology.builder().id(2L).name("Go").description("x").build();
        TechnologyCatalogEvent event = new TechnologyCatalogEvent(2L, "Go");
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(mockSendResult());

        when(mapper.toEvent(tech)).thenReturn(event);
        when(kafkaTemplate.send("sync.technologies.deleted", event)).thenReturn(future);

        StepVerifier.create(adapter.publishDeleted(tech))
            .verifyComplete();
    }
}
