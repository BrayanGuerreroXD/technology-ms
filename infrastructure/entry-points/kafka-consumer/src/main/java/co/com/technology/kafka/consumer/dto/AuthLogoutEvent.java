package co.com.technology.kafka.consumer.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthLogoutEvent {
    private String email;
    private String token;
}
