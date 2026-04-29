package co.com.technology.kafka.consumer.mapper;

import co.com.technology.kafka.consumer.dto.AuthLoginEvent;
import co.com.technology.model.auth.Auth;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthEventMapper {
    Auth toAuth(AuthLoginEvent event);
}
