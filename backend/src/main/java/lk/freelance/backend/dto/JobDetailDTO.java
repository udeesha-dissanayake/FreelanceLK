package lk.freelance.backend.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDetailDTO {
    private UUID jobId;
    private UUID listingId;
    private String title;
    private String description;
    private String status; // Ensure this exists
    private Integer viewsCount;
    private Boolean isFeatured;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String employmentType;
    private BigDecimal hourlyRate;
    private BigDecimal monthlySalary;
    private Integer hoursPerWeek;
    private Integer durationMonths;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Boolean isRemote;
    private String requirements;
    private String responsibilities;

    // FIX: Add this field for the Mapper to work
    private SellerDetailDTO seller;

    private List<String> mediaUrls;
}