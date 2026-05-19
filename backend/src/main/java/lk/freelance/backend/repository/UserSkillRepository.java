package lk.freelance.backend.repository;

import lk.freelance.backend.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    Optional<UserSkill> findByUser_UserIdAndSkill_SkillId(UUID userId, UUID skillId);
    boolean existsByUser_UserIdAndSkill_SkillId(UUID userId, UUID skillId);

    // FIX: Add this method
    List<UserSkill> findByUser_UserId(UUID userId);
}