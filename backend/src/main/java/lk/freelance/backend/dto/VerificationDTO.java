package lk.freelance.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDTO {
    private UUID verificationId;
    private UUID userId; // Ensure this is named 'userId'
    private String verificationType;
    private String documentNumber;
    private String documentUrl;
    private String verificationStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime verifiedAt;
    private String verifiedBy;
    private String notes;
}