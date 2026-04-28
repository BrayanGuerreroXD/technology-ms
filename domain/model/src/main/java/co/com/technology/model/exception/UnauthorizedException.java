package co.com.technology.model.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final GlobalExceptionEnum error;

    public UnauthorizedException(GlobalExceptionEnum error) {
        super(error.getMessage());
        this.error = error;
    }
}
