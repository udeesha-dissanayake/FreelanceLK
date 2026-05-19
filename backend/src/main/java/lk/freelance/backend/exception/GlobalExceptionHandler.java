package lk.freelance.backend.exception;

import lk.freelance.backend.dto.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode; // Use this instead of HttpStatus for overrides
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(FreelanceLKException.class)
    public ResponseEntity<ErrorResponse> handleFreelanceLKException(FreelanceLKException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error(ex.getError())
                .status(ex.getStatus().value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, ex.getStatus());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error("RESOURCE_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // FIXED: Updated signature for Spring Boot 3 / Spring 6
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .error("VALIDATION_ERROR")
                .status(status.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .errors(validationErrors)
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Invalid email or password")
                .error("AUTHENTICATION_FAILED")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("You do not have permission to access this resource")
                .error("ACCESS_DENIED")
                .status(HttpStatus.FORBIDDEN.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("User not found")
                .error("USER_NOT_FOUND")
                .status(HttpStatus.NOT_FOUND.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .error("INVALID_ARGUMENT")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("An unexpected error occurred.")
                .error("INTERNAL_SERVER_ERROR")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(request.getDescription(false).replace("uri=", ""))
                .timestamp(LocalDateTime.now())
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}