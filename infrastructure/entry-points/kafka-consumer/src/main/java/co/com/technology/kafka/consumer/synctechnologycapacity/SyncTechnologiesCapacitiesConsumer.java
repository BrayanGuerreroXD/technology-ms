package co.com.technology.kafka.consumer.synctechnologycapacity;

import co.com.technology.kafka.consumer.dto.SyncTechnologiesCapacitiesEvent;
import co.com.technology.usecase.synctechnologycapacity.SyncTechnologyCapacityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncTechnologiesCapacitiesConsumer {

    private final SyncTechnologyCapacityService syncTechnologyCapacityService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.sync-technologies-capacities-match}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            SyncTechnologiesCapacitiesEvent event = objectMapper.readValue(record.value(), SyncTechnologiesCapacitiesEvent.class);
            syncTechnologyCapacityService.sync(event.getCapacityId(), event.getTechnologyIds())
                .subscribe(null, error -> log.error("Error syncing technologies capacities for {}: {}", event.getCapacityId(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing sync.technologies.capacities.match message: {}", e.getMessage());
        }
    }
}