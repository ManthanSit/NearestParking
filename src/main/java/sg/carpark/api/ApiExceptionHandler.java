package sg.carpark.api;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sg.carpark.catalog.CatalogRefreshCooldownException;
import sg.carpark.catalog.CatalogRefreshFailedException;
import sg.carpark.search.NearestCarParkService;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class,
            MissingServletRequestParameterException.class, HandlerMethodValidationException.class, TypeMismatchException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception exception) {
        return ResponseEntity.badRequest().body(new ErrorResponse("bad_request", exception.getMessage()));
    }

    @ExceptionHandler(NearestCarParkService.DataNotReadyException.class)
    public ResponseEntity<ErrorResponse> notReady(Exception exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("data_not_ready", exception.getMessage()));
    }

    @ExceptionHandler(CatalogRefreshCooldownException.class)
    public ResponseEntity<ErrorResponse> cooldown(CatalogRefreshCooldownException exception) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", Long.toString(exception.retryAfterSeconds()))
                .body(new ErrorResponse("refresh_cooldown", exception.getMessage()));
    }

    @ExceptionHandler(CatalogRefreshFailedException.class)
    public ResponseEntity<ErrorResponse> upstream(CatalogRefreshFailedException exception) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("upstream_error", "The catalogue refresh failed"));
    }

    public record ErrorResponse(String code, String message) {}
}
