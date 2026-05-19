package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.MessageDTO;
import lk.freelance.backend.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public MessageDTO toDTO(Message message) {
        if (message == null) return null;

        return MessageDTO.builder()
                .messageId(message.getMessageId())
                .conversationId(message.getConversation().getConversationId())
                .senderId(message.getSender().getUserId())
                .senderName(message.getSender().getUserProfile() != null ?
                        message.getSender().getUserProfile().getDisplayName() : "User")
                .content(message.getContent())
                .messageType(message.getMessageType() != null ? message.getMessageType().name() : "TEXT")
                .attachmentUrl(message.getAttachmentUrl())
                .isRead(message.getIsRead())
                .createdAt(message.getCreatedAt())
                .build();
    }
}