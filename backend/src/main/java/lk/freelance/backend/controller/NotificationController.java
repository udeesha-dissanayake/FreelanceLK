// ================================================================
// NOTIFICATION CONTROLLER
// FreelanceLK.com - User Notifications
// ================================================================
// Handles bell-icon notifications: read, mark read, delete
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-08
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.ApiResponse;
import lk.freelance.backend.dto.NotificationDTO;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for user notifications
 * Base URL: /api/v1/notifications
 *
 * @apiNote All endpoints require authentication — users can only
 *          access their own notifications.
 * @security isAuthenticated() — ownership enforced in service layer
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class NotificationController {

    private final NotificationService notificationService;

    // ================================================================
    // GET /api/v1/notifications
    // Get all notifications for current user (newest first)
    // ================================================================

    /**
     * Returns the full notification history for the authenticated user.
     * Frontend should use this for the "Notifications" page.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getMyNotifications(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<NotificationDTO> notifications =
                notificationService.getNotificationsForUser(currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    // ================================================================
    // GET /api/v1/notifications/unread
    // Get only unread notifications (used for bell-icon dropdown)
    // ================================================================

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<NotificationDTO> unread =
                notificationService.getUnreadNotificationsForUser(currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success(unread));
    }

    // ================================================================
    // GET /api/v1/notifications/unread-count
    // Get unread count (lightweight poll for badge number)
    // ================================================================

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @CurrentUser UserPrincipal currentUser
    ) {
        long count = notificationService.countUnread(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ================================================================
    // PATCH /api/v1/notifications/{notificationId}/read
    // Mark a single notification as read
    // ================================================================

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<NotificationDTO>> markAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID notificationId
    ) {
        NotificationDTO updated =
                notificationService.markAsRead(notificationId, currentUser.getUserId());

        return ResponseEntity.ok(ApiResponse.success("Notification marked as read.", updated));
    }

    // ================================================================
    // PATCH /api/v1/notifications/read-all
    // Mark all notifications as read in one shot
    // ================================================================

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @CurrentUser UserPrincipal currentUser
    ) {
        notificationService.markAllAsRead(currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read."));
    }

    // ================================================================
    // DELETE /api/v1/notifications/{notificationId}
    // Delete a single notification
    // ================================================================

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID notificationId
    ) {
        notificationService.deleteNotification(notificationId, currentUser.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted."));
    }
}
