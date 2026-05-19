// ================================================================
// REFRESH TOKEN REQUEST DTO
// FreelanceLK.com - Token Refresh
// ================================================================
// Request body for token refresh
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO for refresh token request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}