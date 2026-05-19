// ================================================================
// NOTIFICATION DTO
// FreelanceLK.com - Notification Information
// ================================================================
// Information about user notifications
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for notification information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private UUID notificationId;
    private String type;
    private String title;
    private String message;
    private String link;
    private Boolean isRead;
    private LocalDateTime createdAt;
}