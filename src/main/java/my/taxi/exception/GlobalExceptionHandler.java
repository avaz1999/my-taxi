package my.taxi.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import my.taxi.base.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * DTO validatsiya xatolari (@Valid / @NotBlank / @Size ...)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Response<ApiError>> handleValidation(MethodArgumentNotValidException ex) {
        List<String> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .toList();

        ApiError apiError = ApiError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .details(violations) // agar details qo‘shsang
                .build();

        Response<ApiError> body = Response.fail(
                HttpStatus.BAD_REQUEST.value(),
                apiError,
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * ConstraintViolationException (masalan, @Validated service-level)
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Response<ApiError>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> violations = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + " " + cv.getMessage())
                .toList();

        ApiError apiError = ApiError.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message("Constraint violation")
                .details(violations) // agar details maydonini qo‘shsang
                .build();

        Response<ApiError> body = Response.fail(
                HttpStatus.BAD_REQUEST.value(),
                apiError.getMessage(),
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Boshqa barcha Exception lar
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response<ApiError>> handleAll(Exception ex) {
        String errorId = UUID.randomUUID().toString();

        log.error("Unexpected error [{}]: {}", errorId, ex.getMessage(), ex);

        Response<ApiError> body = Response.fail(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error. Reference ID: " + errorId,
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.ok(body);
    }

    private String formatFieldError(FieldError fieldError) {
        return fieldError.getField() + " " + fieldError.getDefaultMessage();
    }
}