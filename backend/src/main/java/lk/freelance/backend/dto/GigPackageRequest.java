// ================================================================
// GIG PACKAGE REQUEST DTO
// FreelanceLK.com - Gig Package Creation
// ================================================================
// Request body for gig package creation
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for gig package request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigPackageRequest {

    @NotBlank(message = "Package name is required")
    @Pattern(regexp = "BASIC|STANDARD|PREMIUM", message = "Package must be BASIC, STANDARD, or PREMIUM")
    private String packageName;

    @Size(max = 500)
    private String packageDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "100.0", message = "Price must be at least LKR 100")
    private BigDecimal price;

    @NotNull(message = "Delivery days is required")
    @Min(value = 1)
    @Max(value = 365)
    private Integer deliveryDays;

    @Min(value = 0)
    @Max(value = 20)
    private Integer revisions;
}