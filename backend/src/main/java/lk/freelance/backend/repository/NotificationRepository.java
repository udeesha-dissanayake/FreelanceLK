package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // Required for UPDATE/DELETE
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // Required for transactional safety

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);

    Long countByUser_UserIdAndIsReadFalse(UUID userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
    void markAllAsReadForUser(@Param("userId") UUID userId);
}