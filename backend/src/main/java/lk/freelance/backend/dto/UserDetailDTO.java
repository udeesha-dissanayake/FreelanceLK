// ================================================================
// USER DETAIL DTO
// FreelanceLK.com - Detailed User Information
// ================================================================
// Detailed information for user display in orders
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for user details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String email;
    private BigDecimal trustScore;
}