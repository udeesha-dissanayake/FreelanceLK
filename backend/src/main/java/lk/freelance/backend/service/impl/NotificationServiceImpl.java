// ================================================================
// NOTIFICATION SERVICE IMPL
// FreelanceLK.com - Notification Business Logic
// ================================================================
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-08
// ================================================================

package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.NotificationDTO;
import lk.freelance.backend.entity.Notification;
import lk.freelance.backend.entity.User;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.NotificationRepository;
import lk.freelance.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link NotificationService}.
 *
 * <p>All public methods that write to the DB are {@code @Transactional}.
 * Read methods are {@code readOnly = true} for a small performance win.</p>
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // ================================================================
    // READ
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForUser(UUID userId) {
        return notificationRepository
                .findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForUser(UUID userId) {
        return notificationRepository
                .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(UUID userId) {
        return notificationRepository.countByUser_UserIdAndIsReadFalse(userId);
    }

    // ================================================================
    // WRITE / SEND
    // ================================================================

    @Override
    @Transactional
    public NotificationDTO send(User recipient, String type, String title, String message, UUID relatedEntityId) {
        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);
        return toDTO(saved);
    }

    // ================================================================
    // MUTATIONS
    // ================================================================

    @Override
    @Transactional
    public NotificationDTO markAsRead(UUID notificationId, UUID requestingUserId) {
        Notification notification = findAndVerifyOwner(notificationId, requestingUserId);
        notification.setIsRead(true);
        return toDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId, UUID requestingUserId) {
        Notification notification = findAndVerifyOwner(notificationId, requestingUserId);
        notificationRepository.delete(notification);
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Notification findAndVerifyOwner(UUID notificationId, UUID requestingUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getUserId().equals(requestingUserId)) {
            throw new UnauthorizedException("You do not own this notification");
        }

        return notification;
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .notificationId(n.getNotificationId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
