// ================================================================
// ORDER CONTROLLER
// FreelanceLK.com - Order Management & Transaction Lifecycle
// ================================================================
// Handles order creation, status updates, deliverables, and messaging
// Version: 1.1.0
// Author: FreelanceLK Development Team
// Last Modified: 2026-05-16
// ================================================================

package lk.freelance.backend.controller;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.service.OrderService;
import lk.freelance.backend.security.CurrentUser;
import lk.freelance.backend.security.UserPrincipal;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for order lifecycle management
 * Base URL: /api/v1/orders
 *
 * @apiNote Manages complete order lifecycle from creation to completion
 * @security Order participants only can access their own orders
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class OrderController {

    private final OrderService orderService;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /**
     * Create a new order
     * POST /api/v1/orders
     *
     * @param currentUser Authenticated user (buyer)
     * @param request Order creation details
     * @return Created order with payment information
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> createOrder(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody CreateOrderRequest request
    ) {
        OrderDetailDTO order = orderService.createOrder(
                currentUser.getUserId(),
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order created successfully! Payment is being processed.",
                        order
                ));
    }

    /**
     * Get order details by ID
     * GET /api/v1/orders/{orderId}
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return Detailed order information
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderById(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        OrderDetailDTO order = orderService.getOrderById(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(order));
    }

    /**
     * Get order by order number
     * GET /api/v1/orders/number/{orderNumber}
     *
     * @param currentUser Authenticated user
     * @param orderNumber Order number (e.g., ORD-20260127-000001)
     * @return Order details
     */
    @GetMapping("/number/{orderNumber}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> getOrderByNumber(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable String orderNumber
    ) {
        OrderDetailDTO order = orderService.getOrderByNumber(
                currentUser.getUserId(),
                orderNumber
        );

        return ResponseEntity
                .ok(ApiResponse.success(order));
    }

    /**
     * Get all orders for current user (as buyer or seller)
     * GET /api/v1/orders
     *
     * @param currentUser Authenticated user
     * @param role Filter by role (BUYER or SELLER)
     * @param status Filter by status
     * @param page Page number
     * @param size Page size
     * @return Paginated list of orders
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryDTO>>> getAllOrders(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String role, // "BUYER" or "SELLER"
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<OrderSummaryDTO> orders = orderService.getAllOrders(
                currentUser.getUserId(),
                role,
                status,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(orders));
    }

    /**
     * Get orders as buyer
     * GET /api/v1/orders/purchases
     *
     * @param currentUser Authenticated user
     * @param status Filter by status
     * @param page Page number
     * @param size Page size
     * @return Buyer's orders
     */
    @GetMapping("/purchases")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryDTO>>> getPurchases(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<OrderSummaryDTO> orders = orderService.getBuyerOrders(
                currentUser.getUserId(),
                status,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(orders));
    }

    /**
     * Get orders as seller
     * GET /api/v1/orders/sales
     *
     * @param currentUser Authenticated user
     * @param status Filter by status
     * @param page Page number
     * @param size Page size
     * @return Seller's orders
     */
    @GetMapping("/sales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagedResponse<OrderSummaryDTO>>> getSales(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        PagedResponse<OrderSummaryDTO> orders = orderService.getSellerOrders(
                currentUser.getUserId(),
                status,
                page,
                size
        );

        return ResponseEntity
                .ok(ApiResponse.success(orders));
    }

    /**
     * Accept order (seller action)
     * POST /api/v1/orders/{orderId}/accept
     *
     * @param currentUser Authenticated user (seller)
     * @param orderId Order ID
     * @return Updated order
     */
    @PostMapping("/{orderId}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> acceptOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        OrderDetailDTO order = orderService.acceptOrder(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Order accepted! You can now start working on it.",
                        order
                ));
    }

    /**
     * Start working on order (seller action)
     * POST /api/v1/orders/{orderId}/start
     *
     * @param currentUser Authenticated user (seller)
     * @param orderId Order ID
     * @return Updated order
     */
    @PostMapping("/{orderId}/start")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> startOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        OrderDetailDTO order = orderService.startOrder(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success("Order status updated to In Progress!", order));
    }

    /**
     * Submit deliverable (seller action)
     * POST /api/v1/orders/{orderId}/deliver
     *
     * Flow:
     *   1. Freelancer uploads file via POST /api/v1/files/upload?type=deliverable
     *      → receives { fileUrl, filename }
     *   2. Freelancer calls this endpoint with that fileUrl, fileName, fileSize
     *      → order status moves to DELIVERED, buyer is notified
     *
     * @param currentUser Authenticated user (seller)
     * @param orderId Order ID
     * @param request Deliverable details (fileUrl, fileName, fileSize, description)
     * @return Updated order with deliverable
     */
    @PostMapping("/{orderId}/deliver")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> submitDeliverable(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody SubmitDeliverableRequest request
    ) {
        OrderDetailDTO order = orderService.submitDeliverable(
                currentUser.getUserId(),
                orderId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Deliverable submitted! The buyer will review it.",
                        order
                ));
    }

    /**
     * Request revision (buyer action)
     * POST /api/v1/orders/{orderId}/revision
     *
     * @param currentUser Authenticated user (buyer)
     * @param orderId Order ID
     * @param revisionNotes Revision requirements
     * @return Updated order
     */
    @PostMapping("/{orderId}/revision")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> requestRevision(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @RequestParam String revisionNotes
    ) {
        OrderDetailDTO order = orderService.requestRevision(
                currentUser.getUserId(),
                orderId,
                revisionNotes
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Revision requested. The seller will work on the changes.",
                        order
                ));
    }

    /**
     * Complete order (buyer action - accepts deliverable)
     * POST /api/v1/orders/{orderId}/complete
     *
     * @param currentUser Authenticated user (buyer)
     * @param orderId Order ID
     * @return Completed order
     */
    @PostMapping("/{orderId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> completeOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        OrderDetailDTO order = orderService.completeOrder(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Order completed! Payment has been released to the seller. You can now leave a review.",
                        order
                ));
    }

    /**
     * Cancel order
     * POST /api/v1/orders/{orderId}/cancel
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @param reason Cancellation reason
     * @return Cancelled order
     */
    @PostMapping("/{orderId}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderDetailDTO>> cancelOrder(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @RequestParam String reason
    ) {
        OrderDetailDTO order = orderService.cancelOrder(
                currentUser.getUserId(),
                orderId,
                reason
        );

        return ResponseEntity
                .ok(ApiResponse.success("Order cancelled successfully!", order));
    }

    // ================================================================
    // ORDER MESSAGES
    // ================================================================

    /**
     * Get order messages/conversation
     * GET /api/v1/orders/{orderId}/messages
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return List of messages
     */
    @GetMapping("/{orderId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OrderMessageDTO>>> getOrderMessages(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        List<OrderMessageDTO> messages = orderService.getOrderMessages(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(messages));
    }

    /**
     * Send message in order conversation
     * POST /api/v1/orders/{orderId}/messages
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @param request Message details
     * @return Sent message
     */
    @PostMapping("/{orderId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderMessageDTO>> sendOrderMessage(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody SendOrderMessageRequest request
    ) {
        OrderMessageDTO message = orderService.sendOrderMessage(
                currentUser.getUserId(),
                orderId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success("Message sent!", message));
    }

    /**
     * Mark messages as read
     * POST /api/v1/orders/{orderId}/messages/read
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return Success message
     */
    @PostMapping("/{orderId}/messages/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markMessagesAsRead(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        orderService.markMessagesAsRead(currentUser.getUserId(), orderId);

        return ResponseEntity
                .ok(ApiResponse.success("Messages marked as read!"));
    }

    // ================================================================
    // ORDER DELIVERABLES
    // ================================================================

    /**
     * Get order deliverables
     * GET /api/v1/orders/{orderId}/deliverables
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return List of deliverables
     */
    @GetMapping("/{orderId}/deliverables")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<DeliverableDTO>>> getOrderDeliverables(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        List<DeliverableDTO> deliverables = orderService.getOrderDeliverables(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(deliverables));
    }

    /**
     * Get a temporary download URL for a deliverable.
     * GET /api/v1/orders/{orderId}/deliverables/{deliverableId}/download
     *
     * Returns a short-lived URL pointing to the protected file endpoint below.
     * The client should immediately follow this URL (with the auth header) to
     * trigger the actual file download.
     *
     * @param currentUser   Authenticated user (must be buyer or seller of the order)
     * @param orderId       Order ID
     * @param deliverableId Deliverable ID
     * @return DownloadUrlDTO with url + expiresAt
     */
    @GetMapping("/{orderId}/deliverables/{deliverableId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DownloadUrlDTO>> getDeliverableDownloadUrl(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @PathVariable UUID deliverableId
    ) {
        DownloadUrlDTO downloadUrl = orderService.generateDeliverableDownloadUrl(
                currentUser.getUserId(),
                orderId,
                deliverableId
        );

        return ResponseEntity
                .ok(ApiResponse.success(downloadUrl));
    }

    /**
     * Download deliverable file directly (streams the file to the client).
     * GET /api/v1/orders/{orderId}/deliverables/{deliverableId}/file
     *
     * This is the ONLY way to download a deliverable file. Access is restricted
     * to the buyer and seller of the order (enforced by OrderService).
     * The response is always "attachment" — forcing a browser download.
     *
     * Typical client flow:
     *   1. GET /deliverables/{id}/download  → get the DownloadUrlDTO
     *   2. GET /deliverables/{id}/file      → receive the actual file bytes
     *      (Authorization header required)
     *
     * @param currentUser   Authenticated user (must be buyer or seller of the order)
     * @param orderId       Order ID
     * @param deliverableId Deliverable ID
     * @return File bytes with Content-Disposition: attachment
     */
    @GetMapping("/{orderId}/deliverables/{deliverableId}/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> downloadDeliverableFile(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @PathVariable UUID deliverableId
    ) {
        // Verify the caller is the buyer or seller of this order,
        // and that the deliverable belongs to the order.
        DownloadUrlDTO dto = orderService.generateDeliverableDownloadUrl(
                currentUser.getUserId(),
                orderId,
                deliverableId
        );

        // Extract the filename from the stored URL
        // e.g. "/api/v1/files/deliverable/userId_uuid.zip" → "userId_uuid.zip"
        String storedUrl = dto.getDownloadUrl();
        String filename = storedUrl.substring(storedUrl.lastIndexOf('/') + 1);

        // Resolve the file on disk
        Path filePath = Paths.get(uploadDir)
                .resolve("deliverable")
                .resolve(filename)
                .toAbsolutePath()
                .normalize();

        try {
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Deliverable file not found on disk: " + filename);
            }

            // Detect content type for the response
            String contentType = detectContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    // "attachment" forces browser download instead of inline preview
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Deliverable file not found: " + filename);
        }
    }

    // ================================================================
    // ORDER STATISTICS & ANALYTICS
    // ================================================================

    /**
     * Get order statistics for current user
     * GET /api/v1/orders/stats
     *
     * @param currentUser Authenticated user
     * @return Order statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderStatisticsDTO>> getOrderStatistics(
            @CurrentUser UserPrincipal currentUser
    ) {
        OrderStatisticsDTO stats = orderService.getOrderStatistics(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(stats));
    }

    /**
     * Get active orders count
     * GET /api/v1/orders/active/count
     *
     * @param currentUser Authenticated user
     * @return Active orders count
     */
    @GetMapping("/active/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OrderCountDTO>> getActiveOrdersCount(
            @CurrentUser UserPrincipal currentUser
    ) {
        OrderCountDTO count = orderService.getActiveOrdersCount(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(count));
    }

    /**
     * Get pending actions (orders requiring attention)
     * GET /api/v1/orders/pending-actions
     *
     * @param currentUser Authenticated user
     * @return List of orders requiring action
     */
    @GetMapping("/pending-actions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OrderSummaryDTO>>> getPendingActions(
            @CurrentUser UserPrincipal currentUser
    ) {
        List<OrderSummaryDTO> pendingOrders = orderService.getPendingActions(
                currentUser.getUserId()
        );

        return ResponseEntity
                .ok(ApiResponse.success(pendingOrders));
    }

    // ================================================================
    // DISPUTE MANAGEMENT
    // ================================================================

    /**
     * Raise a dispute for an order
     * POST /api/v1/orders/{orderId}/dispute
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @param request Dispute details
     * @return Created dispute
     */
    @PostMapping("/{orderId}/dispute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DisputeDTO>> raiseDispute(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId,
            @Valid @RequestBody RaiseDisputeRequest request
    ) {
        DisputeDTO dispute = orderService.raiseDispute(
                currentUser.getUserId(),
                orderId,
                request
        );

        return ResponseEntity
                .ok(ApiResponse.success(
                        "Dispute raised successfully! Our team will review it within 24 hours.",
                        dispute
                ));
    }

    /**
     * Get dispute for an order
     * GET /api/v1/orders/{orderId}/dispute
     *
     * @param currentUser Authenticated user
     * @param orderId Order ID
     * @return Dispute details
     */
    @GetMapping("/{orderId}/dispute")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DisputeDTO>> getOrderDispute(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable UUID orderId
    ) {
        DisputeDTO dispute = orderService.getOrderDispute(
                currentUser.getUserId(),
                orderId
        );

        return ResponseEntity
                .ok(ApiResponse.success(dispute));
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private String detectContentType(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        String ext = (dotIndex >= 0) ? filename.substring(dotIndex + 1).toLowerCase() : "";
        return switch (ext) {
            case "zip"  -> "application/zip";
            case "rar"  -> "application/x-rar-compressed";
            case "pdf"  -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "doc"  -> "application/msword";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "mp4"  -> "video/mp4";
            case "mov"  -> "video/quicktime";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"  -> "image/png";
            default     -> "application/octet-stream";
        };
    }
}
