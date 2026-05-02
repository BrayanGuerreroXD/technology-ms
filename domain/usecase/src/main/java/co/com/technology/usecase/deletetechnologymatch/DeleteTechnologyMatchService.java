package co.com.technology.usecase.deletetechnologymatch;

import reactor.core.publisher.Mono;

import java.util.List;

public interface DeleteTechnologyMatchService {
    Mono<Void> delete(List<Long> technologyIds);
}