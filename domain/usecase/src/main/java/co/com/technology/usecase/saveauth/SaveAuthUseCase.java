package co.com.technology.usecase.saveauth;

import co.com.technology.model.auth.Auth;
import co.com.technology.model.auth.gateways.AuthRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class SaveAuthUseCase implements SaveAuthService {

    private final AuthRepository authRepository;

    @Override
    public Mono<Auth> save(Auth auth) {
        Auth toSave = auth.toBuilder().createdAt(LocalDateTime.now()).build();
        return authRepository.findByEmail(auth.getEmail())
            .flatMap(existing -> authRepository.deleteByEmail(existing.getEmail()))
            .then(authRepository.save(toSave));
    }
}
