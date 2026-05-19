package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// CHANGED: javax -> jakarta
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for raising a dispute
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RaiseDisputeRequest {

    @NotBlank(message = "Reason is required")
    @Size(max = 255)
    private String reason;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 2000, message = "Description must be between 50 and 2000 characters")
    private String description;
}