// ================================================================
// USER SUMMARY DTO
// FreelanceLK.com - Basic User Information
// ================================================================
// Basic information for user display in listings
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for user summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String avatarUrl;
}