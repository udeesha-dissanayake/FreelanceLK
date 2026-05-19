package lk.freelance.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // 1. Success with only data
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 2. Success with message AND data (for register/login)
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 3. Success with ONLY message (Fixes the <Void> errors)
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}