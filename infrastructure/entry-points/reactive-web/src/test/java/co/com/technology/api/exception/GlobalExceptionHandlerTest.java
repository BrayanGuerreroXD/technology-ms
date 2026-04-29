package co.com.technology.api.exception;

import co.com.technology.model.exception.BadRequestException;
import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

class GlobalExceptionHandlerTest {

    private WebTestClient clientFor(Throwable error) {
        var handler = new GlobalExceptionHandler(new ObjectMapper());
        var httpHandler = WebHttpHandlerBuilder
            .webHandler(exchange -> Mono.error(error))
            .exceptionHandler(handler)
            .build();
        return WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldReturn400_forBadRequestException() {
        clientFor(new BadRequestException(GlobalExceptionEnum.INVALID_TECHNOLOGY_ID))
            .get().uri("/any").exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("INVALID_TECHNOLOGY_ID");
    }

    @Test
    void shouldReturn404_forNotFoundException() {
        clientFor(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND))
            .get().uri("/any").exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NOT_FOUND");
    }

    @Test
    void shouldReturn409_forConflictException() {
        clientFor(new ConflictException(GlobalExceptionEnum.TECHNOLOGY_NAME_ALREADY_EXISTS))
            .get().uri("/any").exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NAME_ALREADY_EXISTS");
    }

    @Test
    void shouldReturn401_forUnauthorizedException() {
        clientFor(new UnauthorizedException(GlobalExceptionEnum.UNAUTHORIZED))
            .get().uri("/any").exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("UNAUTHORIZED");
    }

    @Test
    void shouldReturn500_forUnknownException() {
        clientFor(new RuntimeException("unexpected"))
            .get().uri("/any").exchange()
            .expectStatus().isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("UNKNOWN")
            .jsonPath("$.data.message").isEqualTo("Internal Server Error");
    }
}
