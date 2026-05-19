// ================================================================
// CREATE JOB REQUEST DTO
// FreelanceLK.com - Job Creation
// ================================================================
// Request body for creating a new part-time job
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating job request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 10, max = 255, message = "Title must be between 10 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 50, max = 5000, message = "Description must be between 50 and 5000 characters")
    private String description;

    @NotNull(message = "Employment type is required")
    private String employmentType; // "PART_TIME", "CONTRACT", "FREELANCE"

    @DecimalMin(value = "500.0", message = "Hourly rate must be at least LKR 500")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "10000.0", message = "Monthly salary must be at least LKR 10,000")
    private BigDecimal monthlySalary;

    @Min(value = 1, message = "Hours per week must be at least 1")
    @Max(value = 40, message = "Hours per week must not exceed 40")
    private Integer hoursPerWeek;

    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 24, message = "Duration must not exceed 24 months")
    private Integer durationMonths;

    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 255)
    private String location;

    private Boolean isRemote;

    @Size(max = 2000)
    private String requirements;

    @Size(max = 2000)
    private String responsibilities;

    private List<UUID> requiredSkillIds;

    private List<String> mediaUrls;
}