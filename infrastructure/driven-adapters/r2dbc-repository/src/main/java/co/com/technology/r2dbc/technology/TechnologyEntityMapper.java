package co.com.technology.r2dbc.technology;

import co.com.technology.model.technology.Technology;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyEntityMapper {
    Technology toModel(TechnologyEntity entity);
    TechnologyEntity toEntity(Technology model);
}
