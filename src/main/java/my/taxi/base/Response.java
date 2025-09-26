package my.taxi.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import my.taxi.exception.ApiError;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<R> implements Serializable {

    private final String id;
    private final boolean success;
    private final R result;
    private final ApiError error;

    @JsonIgnore
    private final HttpStatus status;

    // PUBLIC 5-arg konstruktor + shu orqali Builder yaratiladi
    @Builder
    public Response(String id,
                    boolean success,
                    R result,
                    ApiError error,
                    HttpStatus status) {
        this.id = id;
        this.success = success;
        this.result = result;
        this.error = error;
        this.status = status;
    }

    /* --- Qulay helperlar (ixtiyoriy, lekin foydali) --- */

    public static <R extends Serializable> Response<R> ok(R data) {
        return Response.<R>builder()
                .success(true)
                .result(data)
                .status(HttpStatus.OK)
                .build();
    }

    public static <R extends Serializable> Response<R> ok(String id, R data) {
        return Response.<R>builder()
                .id(id)
                .success(true)
                .result(data)
                .status(HttpStatus.OK)
                .build();
    }

    public static <R extends Serializable> Response<R> fail(int code, String message, HttpStatus status) {
        return Response.<R>builder()
                .success(false)
                .error(ApiError.builder().code(code).message(message).build())
                .status(status)
                .build();
    }

    public static <R extends Serializable> Response<R> fail(int code, ApiError error, HttpStatus status) {
        return Response.<R>builder()
                .success(false)
                .error(ApiError.builder()
                        .code(code)
                        .message(error.getMessage())
                        .details(error.getDetails()).build())
                .status(status)
                .build();
    }
}
