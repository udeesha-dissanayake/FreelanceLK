// ================================================================
// DISPUTE DTO
// FreelanceLK.com - Dispute Information
// ================================================================
// Contains dispute details and resolution
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for dispute information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeDTO {
    private UUID disputeId;
    private UUID orderId;
    private String orderNumber;
    private UUID raisedBy;
    private String raisedByName;
    private String reason;
    private String description;
    private String status;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}