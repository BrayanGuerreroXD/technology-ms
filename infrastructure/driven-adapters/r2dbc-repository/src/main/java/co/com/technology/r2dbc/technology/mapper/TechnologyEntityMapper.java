package co.com.technology.r2dbc.technology.mapper;

import co.com.technology.model.technology.Technology;
import co.com.technology.r2dbc.technology.entity.TechnologyEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyEntityMapper {
    Technology toModel(TechnologyEntity entity);
    TechnologyEntity toEntity(Technology model);
}
