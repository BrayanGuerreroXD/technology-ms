package co.com.technology.api.security;

import co.com.technology.usecase.getauth.GetAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthSecurityContextRepository implements ServerSecurityContextRepository {

    private final GetAuthService getAuthService;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty();
        }
        String token = authHeader.substring(7);
        return getAuthService.getByToken(token)
            .map(loggedUser -> {
                var auth = new UsernamePasswordAuthenticationToken(loggedUser, null, List.of());
                return (SecurityContext) new SecurityContextImpl(auth);
            })
            .onErrorResume(e -> Mono.empty());
    }
}
