package co.com.technology.api.technology;

import co.com.technology.api.technology.dto.TechnologyRequest;
import co.com.technology.api.technology.dto.TechnologyResponse;
import co.com.technology.usecase.createtechnology.CreateTechnologyService;
import co.com.technology.usecase.deletetechnology.DeleteTechnologyService;
import co.com.technology.usecase.gettechnology.GetTechnologyService;
import co.com.technology.usecase.updatetechnology.UpdateTechnologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TechnologyHandler {

    private final CreateTechnologyService createService;
    private final UpdateTechnologyService updateService;
    private final GetTechnologyService getService;
    private final DeleteTechnologyService deleteService;
    private final TechnologyDTOMapper mapper;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(TechnologyRequest.class)
            .flatMap(dto -> createService.create(mapper.toModel(dto)))
            .flatMap(tech -> ServerResponse.ok().bodyValue(mapper.toResponse(tech)));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return request.bodyToMono(TechnologyRequest.class)
            .flatMap(dto -> updateService.update(id, mapper.toModel(dto)))
            .flatMap(tech -> ServerResponse.ok().bodyValue(mapper.toResponse(tech)));
    }

    public Mono<ServerResponse> getById(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return getService.getById(id)
            .flatMap(tech -> ServerResponse.ok().bodyValue(mapper.toResponse(tech)));
    }

    public Mono<ServerResponse> getAll(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        return ServerResponse.ok()
            .body(getService.getAll(page, size).map(mapper::toResponse), TechnologyResponse.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.valueOf(request.pathVariable("id"));
        return deleteService.delete(id)
            .then(ServerResponse.noContent().build());
    }
}
