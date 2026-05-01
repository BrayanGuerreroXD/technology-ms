package co.com.technology.kafka.consumer.synctechnologycapacity;

import co.com.technology.kafka.consumer.dto.SyncTechnologiesCapacitiesEvent;
import co.com.technology.usecase.synctechnologycapacity.SyncTechnologyCapacityService;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncTechnologiesCapacitiesConsumerTest {

    @Mock
    private SyncTechnologyCapacityService syncTechnologyCapacityService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SyncTechnologiesCapacitiesConsumer consumer;

    @Test
    void consume_parsesEventAndCallsSync() throws Exception {
        String json = "{\"capacityId\":1,\"technologyIds\":[10,20,30]}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("sync.technologies.capacities.match", 0, 0, null, json);
        SyncTechnologiesCapacitiesEvent event = new SyncTechnologiesCapacitiesEvent(1L, List.of(10L, 20L, 30L));

        when(objectMapper.readValue(json, SyncTechnologiesCapacitiesEvent.class)).thenReturn(event);
        when(syncTechnologyCapacityService.sync(1L, List.of(10L, 20L, 30L))).thenReturn(Mono.empty());

        consumer.consume(record);

        verify(syncTechnologyCapacityService).sync(1L, List.of(10L, 20L, 30L));
    }
}