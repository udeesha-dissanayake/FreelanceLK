// ================================================================
// EMPLOYER DETAIL DTO
// FreelanceLK.com - Complete Employer Information
// ================================================================
// Complete details for employers
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for employer details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerDetailDTO {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private String location;
    private LocalDateTime memberSince;
    private BigDecimal trustScore;
}