package co.com.technology.api.technology;

import co.com.technology.api.technology.dto.TechnologyRequest;
import co.com.technology.api.technology.dto.TechnologyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Configuration
public class TechnologyRouter {

    private static final String PATH = "/api/v1/technologies";

    @RouterOperations({
        @RouterOperation(path = PATH, method = RequestMethod.POST,
                beanClass = TechnologyHandler.class, beanMethod = "create",
                produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE,
                operation = @Operation(
                        operationId = "createTechnology",
                        summary = "Create a new technology",
                        requestBody = @RequestBody(required = true,
                                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = TechnologyRequest.class))),
                        responses = {
                            @ApiResponse(responseCode = "201", description = "Technology created successfully",
                                    content = @Content(schema = @Schema(implementation = TechnologyResponse.class))),
                            @ApiResponse(responseCode = "400", description = "Invalid request data")
                        }
                )),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.PUT,
                beanClass = TechnologyHandler.class, beanMethod = "update",
                produces = MediaType.APPLICATION_JSON_VALUE,
                consumes = MediaType.APPLICATION_JSON_VALUE,
                operation = @Operation(
                        operationId = "updateTechnology",
                        summary = "Update an existing technology",
                        parameters = {
                            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    schema = @Schema(type = "integer", format = "int64"))
                        },
                        requestBody = @RequestBody(required = true,
                                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = TechnologyRequest.class))),
                        responses = {
                            @ApiResponse(responseCode = "200", description = "Technology updated successfully",
                                    content = @Content(schema = @Schema(implementation = TechnologyResponse.class))),
                            @ApiResponse(responseCode = "404", description = "Technology not found")
                        }
                )),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.GET,
                beanClass = TechnologyHandler.class, beanMethod = "getById",
                produces = MediaType.APPLICATION_JSON_VALUE,
                operation = @Operation(
                        operationId = "getTechnologyById",
                        summary = "Get technology by ID",
                        parameters = {
                            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    schema = @Schema(type = "integer", format = "int64"))
                        },
                        responses = {
                            @ApiResponse(responseCode = "200", description = "Technology found",
                                    content = @Content(schema = @Schema(implementation = TechnologyResponse.class))),
                            @ApiResponse(responseCode = "404", description = "Technology not found")
                        }
                )),
        @RouterOperation(path = PATH, method = RequestMethod.GET,
                beanClass = TechnologyHandler.class, beanMethod = "getAll",
                produces = MediaType.APPLICATION_JSON_VALUE,
                operation = @Operation(
                        operationId = "getAllTechnologies",
                        summary = "Get all technologies",
                        responses = {
                            @ApiResponse(responseCode = "200", description = "List of technologies",
                                    content = @Content(schema = @Schema(implementation = TechnologyResponse.class)))
                        }
                )),
        @RouterOperation(path = PATH + "/{id}", method = RequestMethod.DELETE,
                beanClass = TechnologyHandler.class, beanMethod = "delete",
                operation = @Operation(
                        operationId = "deleteTechnology",
                        summary = "Delete a technology",
                        parameters = {
                            @Parameter(name = "id", in = ParameterIn.PATH, required = true,
                                    schema = @Schema(type = "integer", format = "int64"))
                        },
                        responses = {
                            @ApiResponse(responseCode = "204", description = "Technology deleted successfully"),
                            @ApiResponse(responseCode = "404", description = "Technology not found")
                        }
                )),
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
