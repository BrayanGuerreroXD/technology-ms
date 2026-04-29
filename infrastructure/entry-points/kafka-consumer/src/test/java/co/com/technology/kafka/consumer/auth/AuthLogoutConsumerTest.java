package co.com.technology.kafka.consumer.auth;

import co.com.technology.kafka.consumer.dto.AuthLogoutEvent;
import co.com.technology.usecase.deleteauth.DeleteAuthService;
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
class AuthLogoutConsumerTest {

    @Mock
    private DeleteAuthService deleteAuthService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthLogoutConsumer consumer;

    @Test
    void consume_parsesEventAndCallsDelete() throws Exception {
        String json = "{\"email\":\"u@t.com\",\"token\":\"tok\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("generic.auth.logout", 0, 0, null, json);
        AuthLogoutEvent event = new AuthLogoutEvent("u@t.com", "tok");

        when(objectMapper.readValue(json, AuthLogoutEvent.class)).thenReturn(event);
        when(deleteAuthService.delete("u@t.com", "tok")).thenReturn(Mono.empty());

        consumer.consume(record);

        verify(deleteAuthService).delete("u@t.com", "tok");
    }
}
