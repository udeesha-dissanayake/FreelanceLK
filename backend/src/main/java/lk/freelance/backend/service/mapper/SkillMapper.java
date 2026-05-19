package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.SkillDTO;
import lk.freelance.backend.entity.Skill;
import lk.freelance.backend.entity.UserSkill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public SkillDTO toDTO(Skill skill) {
        if (skill == null) return null;

        return SkillDTO.builder()
                .skillId(skill.getSkillId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory())
                .createdAt(skill.getCreatedAt())
                .build();
    }

    public SkillDTO toDTO(UserSkill userSkill) {
        if (userSkill == null) return null;

        Skill skill = userSkill.getSkill();

        return SkillDTO.builder()
                .skillId(skill.getSkillId())
                .skillName(skill.getSkillName())
                .category(skill.getCategory())
                .proficiencyLevel(userSkill.getProficiencyLevel() != null ?
                        String.valueOf(userSkill.getProficiencyLevel()) : null)
                // FIX: Convert BigDecimal to Integer
                .yearsOfExperience(userSkill.getYearsOfExperience() != null ?
                        userSkill.getYearsOfExperience().intValue() : 0)
                .createdAt(userSkill.getCreatedAt())
                .build();
    }
}