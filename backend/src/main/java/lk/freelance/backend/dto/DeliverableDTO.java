package lk.freelance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverableDTO {
    private UUID deliverableId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String description;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;
}