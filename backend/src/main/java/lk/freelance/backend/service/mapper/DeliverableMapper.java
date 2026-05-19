package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.DeliverableDTO; // Import the newly created DTO
import lk.freelance.backend.entity.OrderDeliverable;
import org.springframework.stereotype.Component;

@Component
public class DeliverableMapper {

    public DeliverableDTO toDTO(OrderDeliverable deliverable) {
        if (deliverable == null) {
            return null;
        }

        return DeliverableDTO.builder()
                .deliverableId(deliverable.getDeliverableId())
                .fileUrl(deliverable.getFileUrl())
                .fileName(deliverable.getFileName())
                .fileSize(deliverable.getFileSize())
                .description(deliverable.getDescription())
                .deliveredAt(deliverable.getDeliveredAt())
                .createdAt(deliverable.getCreatedAt())
                .build();
    }
}