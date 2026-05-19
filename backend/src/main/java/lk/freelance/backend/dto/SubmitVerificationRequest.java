// ================================================================
// SUBMIT VERIFICATION REQUEST DTO
// FreelanceLK.com - Identity Verification
// ================================================================
// Request body for submitting verification documents
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

/**
 * DTO for submit verification request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitVerificationRequest {

    @NotBlank(message = "Verification type is required")
    @Pattern(regexp = "NIC|PASSPORT|DRIVING_LICENSE", message = "Invalid verification type")
    private String verificationType;

    @NotBlank(message = "Document number is required")
    @Size(max = 100)
    private String documentNumber;

    @NotBlank(message = "Document URL is required")
    @Size(max = 500)
    private String documentUrl;
}