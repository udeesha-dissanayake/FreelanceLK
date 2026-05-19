// ================================================================
// CATEGORY DTO
// FreelanceLK.com - Listing Categories
// ================================================================
// Represents a category with subcategories and listing count
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for category information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private String name;
    private String displayName;
    private Integer listingCount;
    private List<String> subcategories;
}