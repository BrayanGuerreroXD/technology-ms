package co.com.technology.r2dbc.auth.mapper;

import co.com.technology.model.auth.Auth;
import co.com.technology.r2dbc.auth.entity.AuthEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthEntityMapper {
    AuthEntity toEntity(Auth auth);
    Auth toModel(AuthEntity entity);
}
