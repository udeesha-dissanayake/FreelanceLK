package lk.freelance.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private UUID messageId;
    private UUID conversationId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String messageType;
    private String attachmentUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}