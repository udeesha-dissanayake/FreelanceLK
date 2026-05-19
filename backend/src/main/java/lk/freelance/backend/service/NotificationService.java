// ================================================================
// NOTIFICATION SERVICE (Interface)
// FreelanceLK.com - Notification Business Logic
// ================================================================
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-08
// ================================================================

package lk.freelance.backend.service;

import lk.freelance.backend.dto.NotificationDTO;
import lk.freelance.backend.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for user notification management.
 *
 * <p>Call {@link #send} from any other service (OrderService,
 * AdminService, etc.) whenever a platform event should surface
 * a notification to a user's inbox.</p>
 */
public interface NotificationService {

    // ── Read ──────────────────────────────────────────────────────

    /** All notifications for a user, newest first. */
    List<NotificationDTO> getNotificationsForUser(UUID userId);

    /** Unread-only notifications, newest first. */
    List<NotificationDTO> getUnreadNotificationsForUser(UUID userId);

    /** How many unread notifications the user has. */
    long countUnread(UUID userId);

    // ── Write ─────────────────────────────────────────────────────

    /**
     * Creates and persists a notification for the given user.
     *
     * @param recipient       the user who should receive this notification
     * @param type            notification category, e.g. "ORDER", "PAYMENT", "SYSTEM"
     * @param title           short headline shown in the bell-icon popup
     * @param message         longer body text
     * @param relatedEntityId optional UUID of the related entity (order, listing, …) for deep-linking
     * @return the persisted notification as a DTO
     */
    NotificationDTO send(User recipient, String type, String title, String message, UUID relatedEntityId);

    /** Convenience overload — no related entity. */
    default NotificationDTO send(User recipient, String type, String title, String message) {
        return send(recipient, type, title, message, null);
    }

    // ── Mutations ─────────────────────────────────────────────────

    /** Mark a single notification as read. Returns the updated DTO. */
    NotificationDTO markAsRead(UUID notificationId, UUID requestingUserId);

    /** Mark every unread notification for a user as read in one query. */
    void markAllAsRead(UUID userId);

    /** Delete a single notification (owner-checked). */
    void deleteNotification(UUID notificationId, UUID requestingUserId);
}
