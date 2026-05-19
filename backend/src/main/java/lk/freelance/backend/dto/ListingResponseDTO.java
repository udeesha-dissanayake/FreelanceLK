package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListingResponseDTO {
    private UUID listingId;
    private String type;
    private String status;
    private UUID creatorId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // GIG fields
    private String category;
    private String pricingModel;
    private BigDecimal basePrice;
    private Integer deliveryDays;

    // JOB fields
    private String employmentType;
    private BigDecimal hourlyRate;
    private BigDecimal monthlySalary;
    private String location;
    private Boolean isRemote;
}
