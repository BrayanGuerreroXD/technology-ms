package co.com.technology.kafka.publisher.technology;

import co.com.technology.model.technology.Technology;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TechnologyEventMapper {
    TechnologyCatalogEvent toEvent(Technology technology);
}
