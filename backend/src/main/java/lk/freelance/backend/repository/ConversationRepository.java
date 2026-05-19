// ================================================================
// CONVERSATION REPOSITORY
// FreelanceLK.com - JPA Repository for Conversations
// ================================================================
// Replaces raw EntityManager JPQL queries in ChatController
// Version: 1.0.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-08
// ================================================================

package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for the {@link Conversation} entity.
 *
 * <p>All JPQL that was previously inlined in ChatController's
 * {@code EntityManager} calls lives here instead, giving us
 * testability, cache participation, and a single source of truth.</p>
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // ================================================================
    // FIND ALL CONVERSATIONS FOR A USER
    // Used by: GET /api/v1/chat/conversations
    // ================================================================

    /**
     * Returns every conversation the given user participates in,
     * newest activity first.  NULL lastMessageAt values sort last
     * (conversations that have never had a message appear at the bottom).
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.userOne.userId = :userId
               OR c.userTwo.userId = :userId
            ORDER BY c.lastMessageAt DESC NULLS LAST
            """)
    List<Conversation> findAllByParticipant(@Param("userId") UUID userId);

    // ================================================================
    // FIND EXISTING CONVERSATION BETWEEN TWO USERS
    // Used by: POST /api/v1/chat/conversations/{otherUserId}
    // ================================================================

    /**
     * Looks up a conversation between two specific users regardless of
     * which user is stored as userOne vs userTwo.
     *
     * @return the conversation wrapped in an Optional, or empty if none exists yet
     */
    @Query("""
            SELECT c FROM Conversation c
            WHERE (c.userOne.userId = :userAId AND c.userTwo.userId = :userBId)
               OR (c.userOne.userId = :userBId AND c.userTwo.userId = :userAId)
            """)
    Optional<Conversation> findBetweenUsers(
            @Param("userAId") UUID userAId,
            @Param("userBId") UUID userBId
    );

    // ================================================================
    // COUNT UNREAD MESSAGES ACROSS ALL USER CONVERSATIONS
    // Used by: GET /api/v1/chat/unread-count
    // ================================================================

    /**
     * Counts messages in any of the user's conversations that were
     * sent by the other party and have not yet been read.
     */
    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.sender.userId != :userId
              AND m.isRead = false
              AND (
                    m.conversation.userOne.userId = :userId
                 OR m.conversation.userTwo.userId = :userId
              )
            """)
    Long countUnreadMessagesForUser(@Param("userId") UUID userId);
}
