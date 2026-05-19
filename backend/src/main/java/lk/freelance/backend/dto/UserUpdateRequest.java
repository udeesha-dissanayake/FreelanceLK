// ================================================================
// USER UPDATE REQUEST DTO
// FreelanceLK.com - Profile Update
// ================================================================
// Request body for updating user profile
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO for user update request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    private String firstName;

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    private String lastName;

    @Size(max = 150)
    private String displayName;

    @Size(max = 500)
    private String avatarUrl;

    @Size(max = 1000)
    private String bio;

    @Size(max = 255)
    private String location;

    @Size(max = 50)
    private String timezone;

    @Size(max = 10)
    private String language;
}