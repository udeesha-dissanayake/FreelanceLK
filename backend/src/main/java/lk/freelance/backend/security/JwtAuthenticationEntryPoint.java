package lk.freelance.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.freelance.backend.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * JWT Authentication Entry Point for handling authentication errors
 */
@Component
@Slf4j
@RequiredArgsConstructor // Generates constructor for final fields
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Inject the shared ObjectMapper (Performance + Consistency)
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .message("Unauthorized - You need to log in to access this resource")
                .error("UNAUTHORIZED")
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Use the injected mapper to write the JSON
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}