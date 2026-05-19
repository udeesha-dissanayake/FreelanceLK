package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryDTO {
    private UUID jobId;
    private UUID listingId;
    private String title;

    // FIX: Add this field for the Mapper to work
    private String status;

    private Integer viewsCount;
    private Boolean isFeatured;
    private LocalDateTime createdAt;

    private String employmentType;
    private BigDecimal hourlyRate;
    private BigDecimal monthlySalary;
    private String location;
    private Boolean isRemote;
    private String thumbnailUrl;

    // FIX: Ensure this nested DTO exists
    private SellerSummaryDTO seller;
}