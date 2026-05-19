package lk.freelance.backend.dto;

import lk.freelance.backend.enums.ApplicationStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationDTO {

    private UUID applicationId;
    private UUID listingId;
    private String jobTitle;
    private UserSummaryDTO freelancer;
    private String coverLetter;
    private String expectedRate;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
}
