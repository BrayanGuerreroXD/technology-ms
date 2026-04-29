package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.kafka.consumer.mapper.AuthEventMapper;
import co.com.technology.usecase.saveauth.SaveAuthService;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthLoginConsumer {

    private final SaveAuthService saveAuthService;
    private final AuthEventMapper mapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topics.auth-login-admin}")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            AuthLoginEvent event = objectMapper.readValue(record.value(), AuthLoginEvent.class);
            saveAuthService.save(mapper.toAuth(event))
                .subscribe(null, error -> log.error("Error saving auth login for {}: {}", event.getEmail(), error.getMessage()));
        } catch (Exception e) {
            log.error("Error parsing auth.login.admin message: {}", e.getMessage());
        }
    }
}
