package lk.freelance.backend.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicationRequest {

    @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
    private String coverLetter;

    @Size(max = 100)
    private String expectedRate; // e.g. "500 LKR/hr" or "80000 LKR/month"
}
