// ================================================================
// SEND ORDER MESSAGE REQUEST DTO
// FreelanceLK.com - Order Communication
// ================================================================
// Request body for sending order messages
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.UUID;

/**
 * DTO for send order message request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOrderMessageRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String messageText;

    @Size(max = 500)
    private String attachmentUrl;
}