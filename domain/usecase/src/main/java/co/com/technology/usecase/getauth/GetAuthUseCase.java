package co.com.technology.usecase.getauth;

import co.com.technology.model.auth.gateways.AuthRepository;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.UnauthorizedException;
import co.com.technology.model.security.LoggedUser;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class GetAuthUseCase implements GetAuthService {

    private final AuthRepository authRepository;

    @Override
    public Mono<LoggedUser> getByToken(String token) {
        return authRepository.findByToken(token)
            .switchIfEmpty(Mono.error(new UnauthorizedException(GlobalExceptionEnum.UNAUTHORIZED)))
            .flatMap(auth -> {
                LocalDateTime expiry = auth.getCreatedAt().plusSeconds(auth.getExpiresIn());
                if (LocalDateTime.now().isAfter(expiry)) {
                    return Mono.error(new UnauthorizedException(GlobalExceptionEnum.TOKEN_EXPIRED));
                }
                return Mono.just(LoggedUser.builder().email(auth.getEmail()).build());
            });
    }
}
