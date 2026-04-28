package co.com.technology.api.exception;

import co.com.technology.model.exception.GlobalExceptionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorData {
    private final String errorCode;
    private final String message;
    private final String description;

    public static ErrorData of(GlobalExceptionEnum error) {
        return new ErrorData(error.name(), error.getMessage(), error.getDescription());
    }
}
