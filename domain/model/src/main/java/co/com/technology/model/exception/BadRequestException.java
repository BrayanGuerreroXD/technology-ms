package co.com.technology.model.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final GlobalExceptionEnum error;

    public BadRequestException(GlobalExceptionEnum error) {
        super(error.getMessage());
        this.error = error;
    }
}
