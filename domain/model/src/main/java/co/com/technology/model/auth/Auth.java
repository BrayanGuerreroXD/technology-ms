package co.com.technology.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Auth {
    private Long id;
    private String email;
    private String token;
    private Integer expiresIn;
    private LocalDateTime createdAt;
}
