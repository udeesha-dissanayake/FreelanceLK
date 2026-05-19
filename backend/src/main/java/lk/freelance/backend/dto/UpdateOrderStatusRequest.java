// ================================================================
// UPDATE ORDER STATUS REQUEST DTO
// FreelanceLK.com - Order Status Management
// ================================================================
// Request body for updating order status
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO for updating order status request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private String status; // "ACCEPTED", "IN_PROGRESS", "DELIVERED", "COMPLETED", "CANCELLED"

    @Size(max = 500)
    private String notes;
}