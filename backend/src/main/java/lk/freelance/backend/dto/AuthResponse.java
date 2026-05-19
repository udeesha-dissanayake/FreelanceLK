// ================================================================
// AUTH RESPONSE DTO
// FreelanceLK.com - Authentication Response
// ================================================================
// Response body for authentication operations
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for authentication response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; // seconds
    private UserProfileDTO user;
}