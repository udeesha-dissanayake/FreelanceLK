package lk.freelance.backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class UpdateListingRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Size(max = 20, message = "Pricing model is too long")
    private String pricingModel;

    @DecimalMin(value = "0.0", inclusive = true, message = "Base price must be non-negative")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Delivery days must be at least 1")
    @Max(value = 365, message = "Delivery days must not exceed 365")
    private Integer deliveryDays;

    @Size(max = 20, message = "Employment type is too long")
    private String employmentType;

    @DecimalMin(value = "0.0", inclusive = true, message = "Hourly rate must be non-negative")
    private BigDecimal hourlyRate;

    @DecimalMin(value = "0.0", inclusive = true, message = "Monthly salary must be non-negative")
    private BigDecimal monthlySalary;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private Boolean isRemote;
}
