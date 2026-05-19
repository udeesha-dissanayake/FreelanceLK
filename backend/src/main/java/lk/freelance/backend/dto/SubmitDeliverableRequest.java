// ================================================================
// SUBMIT DELIVERABLE REQUEST DTO
// FreelanceLK.com - Deliverable Submission
// ================================================================
// Request body for submitting deliverables
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for submit deliverable request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitDeliverableRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "File URL is required")
    @Size(max = 500)
    private String fileUrl;

    @NotBlank(message = "File name is required")
    @Size(max = 255)
    private String fileName;

    private Long fileSize;

    @Size(max = 1000)
    private String description;
}