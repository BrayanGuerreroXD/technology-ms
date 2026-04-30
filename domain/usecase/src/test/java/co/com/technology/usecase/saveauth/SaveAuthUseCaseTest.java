package co.com.technology.usecase.saveauth;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveAuthUseCaseTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private SaveAuthUseCase useCase;

    private Auth auth;

    @BeforeEach
    void setUp() {
        auth = Auth.builder()
            .email("user@test.com")
            .token("token123")
            .expiresIn(3600)
            .build();
    }

    @Test
    void save_whenNoExistingRecord_shouldSaveDirectly() {
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.empty());
        when(authRepository.save(any())).thenReturn(Mono.just(auth));

        StepVerifier.create(useCase.save(auth))
            .expectNextMatches(result -> result.getEmail().equals("user@test.com"))
            .verifyComplete();

        verify(authRepository).save(any());
    }

    @Test
    void save_whenExistingRecord_shouldDeleteThenSave() {
        Auth existing = Auth.builder().email("user@test.com").token("old").build();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.just(existing));
        when(authRepository.deleteByEmail("user@test.com")).thenReturn(Mono.empty());
        when(authRepository.save(any())).thenReturn(Mono.just(auth));

        StepVerifier.create(useCase.save(auth))
            .expectNextMatches(result -> result.getEmail().equals("user@test.com"))
            .verifyComplete();

        verify(authRepository).deleteByEmail("user@test.com");
        verify(authRepository).save(any());
    }
}
