package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.VerificationDTO;
import lk.freelance.backend.entity.IdentityVerification;
import org.springframework.stereotype.Component;

@Component
public class VerificationMapper {

    public VerificationDTO toDTO(IdentityVerification verification) {
        if (verification == null) return null;

        return VerificationDTO.builder()
                .verificationId(verification.getVerificationId())
                .userId(verification.getUser().getUserId())
                // FIX: Remove .name() if verificationType is already a String
                .verificationType(verification.getVerificationType())
                .documentNumber(verification.getDocumentNumber())
                .documentUrl(verification.getDocumentUrl())
                // Keep .name() for Status if it is an Enum
                .verificationStatus(verification.getVerificationStatus() != null ?
                        verification.getVerificationStatus().name() : null)
                .submittedAt(verification.getSubmittedAt())
                .verifiedAt(verification.getVerifiedAt())
                .verifiedBy(verification.getVerifiedBy())
                .notes(verification.getNotes())
                .build();
    }
}