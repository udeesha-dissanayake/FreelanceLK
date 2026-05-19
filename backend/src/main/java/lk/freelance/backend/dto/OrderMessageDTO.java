// ================================================================
// ORDER MESSAGE DTO
// FreelanceLK.com - Order Message Information
// ================================================================
// Information about order messages
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for order message
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderMessageDTO {
    private UUID messageId;
    private UUID senderId;
    private String senderName;
    private String messageText;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}