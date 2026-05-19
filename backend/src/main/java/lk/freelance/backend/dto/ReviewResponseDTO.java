// ================================================================
// REVIEW RESPONSE DTO
// FreelanceLK.com - Review Response Information
// ================================================================
// Information about review responses
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for review response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDTO {
    private UUID responseId;
    private String responseText;
    private LocalDateTime createdAt;
}