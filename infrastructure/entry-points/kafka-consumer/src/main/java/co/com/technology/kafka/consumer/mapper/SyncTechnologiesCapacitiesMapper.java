package co.com.technology.kafka.consumer.mapper;

import co.com.technology.kafka.consumer.dto.SyncTechnologiesCapacitiesEvent;
import co.com.technology.model.technologycapacity.TechnologyCapacity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SyncTechnologiesCapacitiesMapper {
    @Mapping(target = "id", ignore = true)
    TechnologyCapacity toTechnologyCapacity(SyncTechnologiesCapacitiesEvent event, Long technologyId);
}