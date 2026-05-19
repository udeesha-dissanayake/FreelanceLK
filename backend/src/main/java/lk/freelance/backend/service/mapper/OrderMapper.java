package lk.freelance.backend.service.mapper;

import lk.freelance.backend.dto.OrderDTO;
import lk.freelance.backend.dto.OrderDetailDTO;
import lk.freelance.backend.dto.OrderSummaryDTO;
import lk.freelance.backend.entity.Order;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final DeliverableMapper deliverableMapper;

    public OrderMapper(DeliverableMapper deliverableMapper) {
        this.deliverableMapper = deliverableMapper;
    }

    public OrderSummaryDTO toSummaryDTO(Order order) {
        return OrderSummaryDTO.builder()
                .orderId(order.getOrderId())
                .listingTitle(order.getListing().getTitle())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                // Use getUserProfile() to match your User entity rename
                .sellerName(order.getSeller().getUserProfile() != null ?
                        order.getSeller().getUserProfile().getDisplayName() : "Seller")
                .thumbnailUrl(order.getListing().getMedia() != null ? order.getListing().getMedia().stream()
                        .filter(m -> m.getIsPrimary())
                        .findFirst()
                        .map(m -> m.getMediaUrl()).orElse(null) : null)
                .build();
    }

    public OrderDetailDTO toDetailDTO(Order order) {
        // FIX: Ensure this method exists for OrderServiceImpl to call
        return OrderDetailDTO.builder()
                .orderId(order.getOrderId())
                .listingTitle(order.getListing().getTitle())
                .amount(order.getAmount())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .deliverables(order.getDeliverables() != null ? order.getDeliverables().stream()
                        .map(deliverableMapper::toDTO)
                        .collect(Collectors.toList()) : null)
                .build();
    }
}