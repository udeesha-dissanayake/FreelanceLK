// ================================================================
// WITHDRAWAL REQUEST DTO
// FreelanceLK.com - Withdrawal Request
// ================================================================
// Request body for withdrawal requests
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO for withdrawal request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.0", message = "Minimum withdrawal amount is LKR 1,000")
    private BigDecimal amount;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100)
    private String bankName;

    @NotBlank(message = "Account number is required")
    @Size(max = 50)
    private String accountNumber;

    @NotBlank(message = "Account name is required")
    @Size(max = 100)
    private String accountName;
}