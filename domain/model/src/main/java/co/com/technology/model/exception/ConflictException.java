package co.com.technology.model.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    private final GlobalExceptionEnum error;

    public ConflictException(GlobalExceptionEnum error) {
        super(error.getMessage());
        this.error = error;
    }
}
