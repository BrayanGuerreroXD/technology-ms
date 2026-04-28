package co.com.technology.api.technology;

import co.com.technology.api.technology.dto.TechnologyRequest;
import co.com.technology.api.technology.dto.TechnologyResponse;
import co.com.technology.model.technology.Technology;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyDTOMapper {
    Technology toModel(TechnologyRequest request);
    TechnologyResponse toResponse(Technology model);
}
