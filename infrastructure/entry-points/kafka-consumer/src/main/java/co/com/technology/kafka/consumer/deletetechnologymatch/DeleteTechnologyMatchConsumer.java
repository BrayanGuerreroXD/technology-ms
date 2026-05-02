package co.com.technology.kafka.consumer.deletetechnologymatch;

import co.com.technology.kafka.consumer.dto.DeleteTechnologyMatchEvent;
import co.com.technology.usecase.deletetechnologymatch.DeleteTechnologyMatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteTechnologyMatchConsumer {

    private final DeleteTechnologyMatchService deleteTechnologyMatchService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.delete-technology-match}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            DeleteTechnologyMatchEvent event = objectMapper.readValue(record.value(), DeleteTechnologyMatchEvent.class);
            deleteTechnologyMatchService.delete(event.getTechnologyIds())
                .subscribe(null, error -> log.error("Error deleting technologies match: {}", error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing delete.technology.match message: {}", e.getMessage());
        }
    }
}