package lk.freelance.backend.repository;

import lk.freelance.backend.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Skill Repository
 */
@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {

    /**
     * Find skill by name (case-insensitive)
     */
    Optional<Skill> findBySkillName(String skillName);

    /**
     * Find skills by category
     */
    List<Skill> findByCategory(String category);

    /**
     * Search skills by name
     */
    @Query("SELECT s FROM Skill s WHERE LOWER(s.skillName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Skill> searchByName(@Param("query") String query);

    /**
     * Check if skill exists by name
     */
    boolean existsBySkillName(String skillName);
}