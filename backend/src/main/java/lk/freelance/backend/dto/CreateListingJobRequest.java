package lk.freelance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateListingJobRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate must be non-negative")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly salary must be non-negative")
    private BigDecimal monthlySalary;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @NotNull(message = "Remote flag is required")
    private Boolean isRemote;
}
