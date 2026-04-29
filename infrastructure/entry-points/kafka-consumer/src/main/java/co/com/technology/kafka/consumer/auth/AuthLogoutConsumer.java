package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLogoutEvent;
import co.com.technology.usecase.deleteauth.DeleteAuthService;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLogoutConsumer {

    private final DeleteAuthService deleteAuthService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.generic-auth-logout}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            AuthLogoutEvent event = objectMapper.readValue(record.value(), AuthLogoutEvent.class);
            deleteAuthService.delete(event.getEmail(), event.getToken())
                .subscribe(null, error -> log.error("Error processing logout for {}: {}", event.getEmail(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing generic.auth.logout message: {}", e.getMessage());
        }
    }
}
