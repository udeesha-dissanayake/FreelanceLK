// ================================================================
// ERROR RESPONSE DTO
// FreelanceLK.com - Error Handling
// ================================================================
// Standardized error response for API
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for error responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private Boolean success = false;
    private String message;
    private String error;
    private Integer status;
    private String path;
    private LocalDateTime timestamp;
    private List<String> errors; // For validation errors
}