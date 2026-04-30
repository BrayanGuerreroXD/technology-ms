package co.com.technology.api.technology;

import co.com.technology.api.exception.GlobalExceptionHandler;
import co.com.technology.api.technology.dto.TechnologyRequest;
import co.com.technology.api.technology.dto.TechnologyResponse;
import co.com.technology.model.exception.ConflictException;
import co.com.technology.model.exception.GlobalExceptionEnum;
import co.com.technology.model.exception.NotFoundException;
import co.com.technology.model.technology.Technology;
import co.com.technology.usecase.createtechnology.CreateTechnologyService;
import co.com.technology.usecase.deletetechnology.DeleteTechnologyService;
import co.com.technology.usecase.gettechnology.GetTechnologyService;
import co.com.technology.usecase.updatetechnology.UpdateTechnologyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TechnologyRouterTest {

    @Mock private CreateTechnologyService createService;
    @Mock private UpdateTechnologyService updateService;
    @Mock private GetTechnologyService getService;
    @Mock private DeleteTechnologyService deleteService;
    @Mock private TechnologyDTOMapper mapper;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        TechnologyHandler handler = new TechnologyHandler(createService, updateService, getService, deleteService, mapper);
        var routes = new TechnologyRouter().technologyRoutes(handler);
        var exceptionHandler = new GlobalExceptionHandler(new ObjectMapper());

        var httpHandler = WebHttpHandlerBuilder
            .webHandler(RouterFunctions.toWebHandler(routes))
            .exceptionHandler(exceptionHandler)
            .build();

        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void POST_technologies_returns200_withTechnologyResponse() {
        Technology domain = Technology.builder().name("Java").description("Language").build();
        Technology saved = domain.toBuilder().id(1L).build();
        TechnologyResponse response = TechnologyResponse.builder()
            .id(1L).name("Java").description("Language")
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(mapper.toModel(any(TechnologyRequest.class))).thenReturn(domain);
        when(createService.create(any(Technology.class))).thenReturn(Mono.just(saved));
        when(mapper.toResponse(any(Technology.class))).thenReturn(response);

        client.post().uri("/api/v1/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TechnologyRequest("Java", "Language"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.name").isEqualTo("Java")
            .jsonPath("$.data.description").isEqualTo("Language");
    }

    @Test
    void POST_technologies_returns409_whenNameAlreadyExists() {
        Technology domain = Technology.builder().name("Java").description("Language").build();

        when(mapper.toModel(any(TechnologyRequest.class))).thenReturn(domain);
        when(createService.create(any(Technology.class)))
            .thenReturn(Mono.error(new ConflictException(GlobalExceptionEnum.TECHNOLOGY_NAME_ALREADY_EXISTS)));

        client.post().uri("/api/v1/technologies")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TechnologyRequest("Java", "Language"))
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NAME_ALREADY_EXISTS")
            .jsonPath("$.data.message").isEqualTo("Technology name already exists");
    }

    @Test
    void GET_technologies_byId_returns200_withTechnologyResponse() {
        Technology found = Technology.builder().id(1L).name("Java").description("Language").build();
        TechnologyResponse response = TechnologyResponse.builder()
            .id(1L).name("Java").description("Language")
            .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(getService.getById(1L)).thenReturn(Mono.just(found));
        when(mapper.toResponse(any(Technology.class))).thenReturn(response);

        client.get().uri("/api/v1/technologies/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.name").isEqualTo("Java");
    }

    @Test
    void GET_technologies_byId_returns404_whenNotFound() {
        when(getService.getById(99L))
            .thenReturn(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)));

        client.get().uri("/api/v1/technologies/99")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NOT_FOUND")
            .jsonPath("$.data.message").isEqualTo("Technology not found");
    }

    @Test
    void GET_technologies_returns200_withList() {
        Technology t1 = Technology.builder().id(1L).name("Java").description("Language").build();
        Technology t2 = Technology.builder().id(2L).name("Kotlin").description("JVM").build();
        TechnologyResponse r1 = TechnologyResponse.builder().id(1L).name("Java").description("Language").build();
        TechnologyResponse r2 = TechnologyResponse.builder().id(2L).name("Kotlin").description("JVM").build();

        when(getService.getAll(0, 10)).thenReturn(Flux.just(t1, t2));
        when(mapper.toResponse(t1)).thenReturn(r1);
        when(mapper.toResponse(t2)).thenReturn(r2);

        client.get().uri("/api/v1/technologies?page=0&size=10")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data").isArray()
            .jsonPath("$.data[0].id").isEqualTo(1)
            .jsonPath("$.data[1].id").isEqualTo(2);
    }

    @Test
    void PUT_technologies_byId_returns200_withUpdatedTechnology() {
        Technology domain = Technology.builder().name("Java Updated").description("Updated desc").build();
        Technology updated = domain.toBuilder().id(1L).build();
        TechnologyResponse response = TechnologyResponse.builder()
            .id(1L).name("Java Updated").description("Updated desc").build();

        when(mapper.toModel(any(TechnologyRequest.class))).thenReturn(domain);
        when(updateService.update(eq(1L), any(Technology.class))).thenReturn(Mono.just(updated));
        when(mapper.toResponse(any(Technology.class))).thenReturn(response);

        client.put().uri("/api/v1/technologies/1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TechnologyRequest("Java Updated", "Updated desc"))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.data.id").isEqualTo(1)
            .jsonPath("$.data.name").isEqualTo("Java Updated");
    }

    @Test
    void PUT_technologies_byId_returns404_whenNotFound() {
        Technology domain = Technology.builder().name("Java").description("Language").build();

        when(mapper.toModel(any(TechnologyRequest.class))).thenReturn(domain);
        when(updateService.update(eq(99L), any(Technology.class)))
            .thenReturn(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)));

        client.put().uri("/api/v1/technologies/99")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new TechnologyRequest("Java", "Language"))
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NOT_FOUND");
    }

    @Test
    void DELETE_technologies_byId_returns204() {
        when(deleteService.delete(1L)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/technologies/1")
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void DELETE_technologies_byId_returns404_whenNotFound() {
        when(deleteService.delete(99L))
            .thenReturn(Mono.error(new NotFoundException(GlobalExceptionEnum.TECHNOLOGY_NOT_FOUND)));

        client.delete().uri("/api/v1/technologies/99")
            .exchange()
            .expectStatus().isNotFound()
            .expectBody()
            .jsonPath("$.data.errorCode").isEqualTo("TECHNOLOGY_NOT_FOUND");
    }
}
