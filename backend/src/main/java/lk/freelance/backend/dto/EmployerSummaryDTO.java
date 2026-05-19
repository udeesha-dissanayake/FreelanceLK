// ================================================================
// EMPLOYER SUMMARY DTO
// FreelanceLK.com - Employer Information
// ================================================================
// Summary information for employers
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for employer summary
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerSummaryDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String location;
    private BigDecimal trustScore;
}