package co.com.technology.model.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final GlobalExceptionEnum error;

    public NotFoundException(GlobalExceptionEnum error) {
        super(error.getMessage());
        this.error = error;
    }
}
