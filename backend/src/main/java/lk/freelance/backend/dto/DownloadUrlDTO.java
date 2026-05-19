// ================================================================
// DOWNLOAD URL DTO
// FreelanceLK.com - File Download Management
// ================================================================
// Contains signed download URL with expiration time
// ================================================================

package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for download URL with expiration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadUrlDTO {
    private String downloadUrl;
    private LocalDateTime expiresAt;
}