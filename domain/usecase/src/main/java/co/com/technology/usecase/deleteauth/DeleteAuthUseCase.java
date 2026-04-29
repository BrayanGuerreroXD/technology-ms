package co.com.technology.usecase.deleteauth;

import co.com.technology.model.auth.gateways.AuthRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteAuthUseCase implements DeleteAuthService {

    private final AuthRepository authRepository;

    @Override
    public Mono<Void> delete(String email, String token) {
        return authRepository.findByEmail(email)
            .filter(auth -> auth.getToken().equals(token))
            .flatMap(auth -> authRepository.deleteByEmail(auth.getEmail()));
    }
}
