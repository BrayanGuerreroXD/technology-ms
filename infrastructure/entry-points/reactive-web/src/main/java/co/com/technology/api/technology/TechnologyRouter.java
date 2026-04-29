package co.com.technology.api.technology;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class TechnologyRouter {

    private static final String PATH = "/api/v1/technologies";

    @RouterOperations({
        @RouterOperation(path = PATH, method = RequestMethod.POST, beanClass = TechnologyHandler.class, beanMethod = "create"),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.PUT, beanClass = TechnologyHandler.class, beanMethod = "update"),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.GET, beanClass = TechnologyHandler.class, beanMethod = "getById"),
        @RouterOperation(path = PATH, method = RequestMethod.GET, beanClass = TechnologyHandler.class, beanMethod = "getAll"),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.DELETE, beanClass = TechnologyHandler.class, beanMethod = "delete"),
    })
    @Bean
    public RouterFunction<ServerResponse> technologyRoutes(TechnologyHandler handler) {
        return RouterFunctions.route()
            .POST(PATH, accept(MediaType.APPLICATION_JSON), handler::create)
            .PUT(PATH + "/{id}", accept(MediaType.APPLICATION_JSON), handler::update)
            .GET(PATH + "/{id}", handler::getById)
            .GET(PATH, handler::getAll)
            .DELETE(PATH + "/{id}", handler::delete)
            .build();
    }
}
