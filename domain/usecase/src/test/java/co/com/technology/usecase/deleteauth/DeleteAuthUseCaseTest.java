package co.com.technology.usecase.deleteauth;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteAuthUseCaseTest {

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private DeleteAuthUseCase useCase;

    @Test
    void delete_whenEmailFoundAndTokenMatches_shouldDeleteByEmail() {
        Auth auth = Auth.builder().email("user@test.com").token("tok123").build();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.just(auth));
        when(authRepository.deleteByEmail("user@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("user@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository).deleteByEmail("user@test.com");
    }

    @Test
    void delete_whenEmailFoundButTokenDoesNotMatch_shouldDoNothing() {
        Auth auth = Auth.builder().email("user@test.com").token("different").build();
        when(authRepository.findByEmail("user@test.com")).thenReturn(Mono.just(auth));

        StepVerifier.create(useCase.delete("user@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository, never()).deleteByEmail(any());
    }

    @Test
    void delete_whenEmailNotFound_shouldDoNothing() {
        when(authRepository.findByEmail("noone@test.com")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.delete("noone@test.com", "tok123"))
            .verifyComplete();

        verify(authRepository, never()).deleteByEmail(any());
    }
}
