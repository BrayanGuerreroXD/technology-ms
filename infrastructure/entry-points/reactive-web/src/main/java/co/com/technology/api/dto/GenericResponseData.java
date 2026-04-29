package co.com.technology.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericResponseData<T> {
    private final T data;

    public static <T> GenericResponseData<T> of(T data) {
        return new GenericResponseData<>(data);
    }
}
