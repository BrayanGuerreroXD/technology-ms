package co.com.technology.usecase.getauth;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import co.com.technology.model.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAuthUseCaseTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private GetAuthUseCase useCase;

    private final String TOKEN = "valid-token";

    @Test
    void getByToken_whenTokenNotFound_shouldThrowUnauthorized() {
        when(authRepository.findByToken(TOKEN)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.getByToken(TOKEN))
            .expectError(UnauthorizedException.class)
            .verify();
    }

    @Test
    void getByToken_whenTokenExpired_shouldThrowUnauthorized() {
        Auth expired = Auth.builder()
            .email("user@test.com")
            .token(TOKEN)
            .expiresIn(60)
            .createdAt(LocalDateTime.now().minusMinutes(5))
            .build();
        when(authRepository.findByToken(TOKEN)).thenReturn(Mono.just(expired));

        StepVerifier.create(useCase.getByToken(TOKEN))
            .expectError(UnauthorizedException.class)
            .verify();
    }

    @Test
    void getByToken_whenTokenValid_shouldReturnLoggedUser() {
        Auth valid = Auth.builder()
            .email("user@test.com")
            .token(TOKEN)
            .expiresIn(3600)
            .createdAt(LocalDateTime.now())
            .build();
        when(authRepository.findByToken(TOKEN)).thenReturn(Mono.just(valid));

        StepVerifier.create(useCase.getByToken(TOKEN))
            .expectNextMatches(user -> user.getEmail().equals("user@test.com"))
            .verifyComplete();
    }
}
