package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.kafka.consumer.mapper.AuthEventMapper;
import co.com.technology.model.auth.Auth;
import co.com.technology.usecase.saveauth.SaveAuthService;
import tools.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthLoginConsumerTest {

    @Mock
    private SaveAuthService saveAuthService;

    @Mock
    private AuthEventMapper mapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthLoginConsumer consumer;

    @Test
    void consume_parsesEventAndCallsSave() throws Exception {
        String json = "{\"email\":\"u@t.com\",\"token\":\"tok\",\"expiresIn\":3600}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("auth.login.admin", 0, 0, null, json);
        AuthLoginEvent event = new AuthLoginEvent("u@t.com", "tok", 3600);
        Auth auth = Auth.builder().email("u@t.com").token("tok").expiresIn(3600).build();

        when(objectMapper.readValue(json, AuthLoginEvent.class)).thenReturn(event);
        when(mapper.toAuth(event)).thenReturn(auth);
        when(saveAuthService.save(auth)).thenReturn(Mono.just(auth));

        consumer.consume(record);

        verify(saveAuthService).save(auth);
    }
}
