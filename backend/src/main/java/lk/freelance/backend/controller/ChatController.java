// ================================================================
// CHAT CONTROLLER
// FreelanceLK.com - Messaging Between Users
// ================================================================
// Handles conversations and messages between clients and freelancers
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-01-30
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.dto.MessageDTO;
import lk.freelance.backend.entity.Conversation;
import lk.freelance.backend.entity.Message;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.enums.MessageType;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.UserRepository;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.service.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST Controller for chat/messaging between users
 * Base URL: /api/v1/chat
 *
 * @apiNote Supports direct messaging between any two users
 * @security Authenticated users only — users can only access their own conversations
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class ChatController {

    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @PersistenceContext
    private EntityManager entityManager;

    // ----------------------------------------------------------------
    // REQUEST BODY — Send Message
    // ----------------------------------------------------------------
    record SendMessageRequest(
            @NotBlank(message = "Message content cannot be empty")
            @Size(max = 5000, message = "Message too long")
            String content,
            String messageType   // optional — defaults to TEXT
    ) {}

    // ================================================================
    // GET /api/v1/chat/conversations
    // Get all conversations for the current user
    // ================================================================
    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ConversationSummary>>> getMyConversations(
            @CurrentUser UserPrincipal currentUser
    ) {
        String jpql = """
                SELECT c FROM Conversation c
                WHERE c.userOne.userId = :userId OR c.userTwo.userId = :userId
                ORDER BY c.lastMessageAt DESC NULLS LAST
                """;

        List<Conversation> conversations = entityManager
                .createQuery(jpql, Conversation.class)
                .setParameter("userId", currentUser.getUserId())
                .getResultList();

        List<ConversationSummary> result = conversations.stream()
                .map(c -> toConversationSummary(c, currentUser.getUserId()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // ================================================================
    // POST /api/v1/chat/conversations/{otherUserId}
    // Start or get existing conversation with another user
    // ================================================================
    @PostMapping("/conversations/{otherUserId}")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<ConversationSummary>> startOrGetConversation(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID otherUserId
    ) {
        if (currentUser.getUserId().equals(otherUserId)) {
            throw new UnauthorizedException("Cannot start a conversation with yourself");
        }

        User me = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if conversation already exists (either direction)
        String jpql = """
                SELECT c FROM Conversation c
                WHERE (c.userOne.userId = :meId AND c.userTwo.userId = :otherId)
                   OR (c.userOne.userId = :otherId AND c.userTwo.userId = :meId)
                """;

        List<Conversation> existing = entityManager
                .createQuery(jpql, Conversation.class)
                .setParameter("meId", me.getUserId())
                .setParameter("otherId", otherUser.getUserId())
                .getResultList();

        Conversation conversation;
        boolean isNew = false;

        if (!existing.isEmpty()) {
            conversation = existing.get(0);
        } else {
            conversation = Conversation.builder()
                    .userOne(me)
                    .userTwo(otherUser)
                    .createdAt(LocalDateTime.now())
                    .build();
            entityManager.persist(conversation);
            isNew = true;
        }

        String msg = isNew ? "Conversation started!" : "Conversation found.";
        return ResponseEntity
                .status(isNew ? HttpStatus.CREATED : HttpStatus.OK)
                .body(ApiResponse.success(msg, toConversationSummary(conversation, me.getUserId())));
    }

    // ================================================================
    // GET /api/v1/chat/conversations/{conversationId}/messages
    // Get all messages in a conversation
    // ================================================================
    @GetMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getMessages(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID conversationId
    ) {
        Conversation conversation = findConversationAndVerifyAccess(conversationId, currentUser.getUserId());

        // Mark unread messages as read
        conversation.getMessages().forEach(m -> {
            if (!m.getSender().getUserId().equals(currentUser.getUserId()) && Boolean.FALSE.equals(m.getIsRead())) {
                m.setIsRead(true);
            }
        });

        List<MessageDTO> messages = conversation.getMessages().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    // ================================================================
    // POST /api/v1/chat/conversations/{conversationId}/messages
    // Send a message in a conversation
    // ================================================================
    @PostMapping("/conversations/{conversationId}/messages")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<MessageDTO>> sendMessage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID conversationId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        Conversation conversation = findConversationAndVerifyAccess(conversationId, currentUser.getUserId());

        User sender = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MessageType type = MessageType.TEXT;
        if (request.messageType() != null) {
            try {
                type = MessageType.valueOf(request.messageType().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // default to TEXT if invalid type sent
            }
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.content())
                .messageType(type)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        entityManager.persist(message);

        // Update conversation preview
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setLastMessagePreview(
                request.content().length() > 80
                        ? request.content().substring(0, 80) + "..."
                        : request.content()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent!", messageMapper.toDTO(message)));
    }

    // ================================================================
    // GET /api/v1/chat/unread-count
    // Get total unread message count for current user
    // ================================================================
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @CurrentUser UserPrincipal currentUser
    ) {
        String jpql = """
                SELECT COUNT(m) FROM Message m
                WHERE m.sender.userId != :userId
                AND m.isRead = false
                AND (m.conversation.userOne.userId = :userId OR m.conversation.userTwo.userId = :userId)
                """;

        Long count = entityManager
                .createQuery(jpql, Long.class)
                .setParameter("userId", currentUser.getUserId())
                .getSingleResult();

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ================================================================
    // HELPERS
    // ================================================================
    private Conversation findConversationAndVerifyAccess(UUID conversationId, UUID userId) {
        Conversation conversation = entityManager.find(Conversation.class, conversationId);
        if (conversation == null) {
            throw new ResourceNotFoundException("Conversation not found");
        }
        boolean isParticipant = conversation.getUserOne().getUserId().equals(userId)
                || conversation.getUserTwo().getUserId().equals(userId);
        if (!isParticipant) {
            throw new UnauthorizedException("You are not part of this conversation");
        }
        return conversation;
    }

    private ConversationSummary toConversationSummary(Conversation c, UUID myId) {
        User otherUser = c.getUserOne().getUserId().equals(myId) ? c.getUserTwo() : c.getUserOne();
        String otherName = otherUser.getUserProfile() != null
                ? otherUser.getUserProfile().getDisplayName()
                : otherUser.getEmail();

        return new ConversationSummary(
                c.getConversationId(),
                otherUser.getUserId(),
                otherName,
                c.getLastMessagePreview(),
                c.getLastMessageAt(),
                c.getCreatedAt()
        );
    }

    // Simple record for conversation list response
    record ConversationSummary(
            UUID conversationId,
            UUID otherUserId,
            String otherUserName,
            String lastMessagePreview,
            LocalDateTime lastMessageAt,
            LocalDateTime createdAt
    ) {}
}
