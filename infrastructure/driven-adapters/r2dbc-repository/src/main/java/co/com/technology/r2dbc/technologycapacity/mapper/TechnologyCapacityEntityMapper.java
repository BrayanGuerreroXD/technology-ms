package co.com.technology.r2dbc.technologycapacity.mapper;

import co.com.technology.model.technologycapacity.TechnologyCapacity;
import co.com.technology.r2dbc.technologycapacity.entity.TechnologyCapacityEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyCapacityEntityMapper {
    TechnologyCapacity toModel(TechnologyCapacityEntity entity);
    TechnologyCapacityEntity toEntity(TechnologyCapacity model);
}