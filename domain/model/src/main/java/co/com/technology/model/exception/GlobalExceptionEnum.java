package co.com.technology.model.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalExceptionEnum {
    TECHNOLOGY_NOT_FOUND("Technology not found", "No technology found with the provided identifier"),
    TECHNOLOGY_NAME_ALREADY_EXISTS("Technology name already exists", "A technology with the provided name already exists"),
    INVALID_TECHNOLOGY_ID("Invalid technology ID", "The provided technology ID is not valid"),
    UNAUTHORIZED("Unauthorized", "Token is missing or invalid"),
    TOKEN_EXPIRED("Token expired", "The provided token has expired"),
    TECHNOLOGY_IN_USE("Technology in use", "Cannot delete technology because it is being used by a capacity");

    private final String message;
    private final String description;
}
