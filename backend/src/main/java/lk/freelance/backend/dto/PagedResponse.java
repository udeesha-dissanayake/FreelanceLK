// ================================================================
// PAGED RESPONSE DTO
// FreelanceLK.com - Pagination Wrapper
// ================================================================
// Generic pagination wrapper for API responses
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic DTO for paginated responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
}