package lk.freelance.backend.service;

import lk.freelance.backend.dto.*;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Core Order Methods
    OrderDetailDTO createOrder(UUID buyerId, CreateOrderRequest request);

    OrderDetailDTO getOrderById(UUID userId, UUID orderId);

    OrderDetailDTO getOrderByNumber(UUID userId, String orderNumber);

    // Pagination & Filtering
    PagedResponse<OrderSummaryDTO> getAllOrders(UUID userId, String role, String status, Integer page, Integer size);

    PagedResponse<OrderSummaryDTO> getBuyerOrders(UUID userId, String status, Integer page, Integer size);

    PagedResponse<OrderSummaryDTO> getSellerOrders(UUID userId, String status, Integer page, Integer size);

    // Lifecycle Actions
    OrderDetailDTO acceptOrder(UUID userId, UUID orderId);

    OrderDetailDTO startOrder(UUID userId, UUID orderId);

    OrderDetailDTO submitDeliverable(UUID userId, UUID orderId, SubmitDeliverableRequest request);

    OrderDetailDTO requestRevision(UUID userId, UUID orderId, String revisionNotes);

    OrderDetailDTO completeOrder(UUID userId, UUID orderId);

    OrderDetailDTO cancelOrder(UUID userId, UUID orderId, String reason);

    // Messaging
    List<OrderMessageDTO> getOrderMessages(UUID userId, UUID orderId);

    OrderMessageDTO sendOrderMessage(UUID userId, UUID orderId, SendOrderMessageRequest request);

    void markMessagesAsRead(UUID userId, UUID orderId);

    // Deliverables
    List<DeliverableDTO> getOrderDeliverables(UUID userId, UUID orderId);

    DownloadUrlDTO generateDeliverableDownloadUrl(UUID userId, UUID orderId, UUID deliverableId);

    // Stats & Actions
    OrderStatisticsDTO getOrderStatistics(UUID userId);

    OrderCountDTO getActiveOrdersCount(UUID userId);

    List<OrderSummaryDTO> getPendingActions(UUID userId);

    // Dispute Management
    DisputeDTO raiseDispute(UUID userId, UUID orderId, RaiseDisputeRequest request);

    DisputeDTO getOrderDispute(UUID userId, UUID orderId);
}