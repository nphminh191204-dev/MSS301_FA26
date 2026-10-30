package fu.de180174.postservice.common;

import feign.FeignException;
import fu.de180174.postservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse> handleValidationException(ValidationException ex) {
        ApiResponse response = new ApiResponse(2, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response); // HTTP 406
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        ApiResponse response = new ApiResponse(2, errorMsg, null);
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(response); // HTTP 406
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ApiResponse response = new ApiResponse(4, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ApiResponse> handleUserNotActive(UserNotActiveException ex) {
        ApiResponse response = new ApiResponse(5, ex.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // HTTP 400
    }

    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<ApiResponse> handleFeignNotFound(FeignException.NotFound ex) {
        ApiResponse response = new ApiResponse(4, "User not found", null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // HTTP 404
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGeneralException(Exception ex) {
        ApiResponse response = new ApiResponse(0, ex.getMessage() != null ? ex.getMessage() : "Internal server error", null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // HTTP 500
    }
}
