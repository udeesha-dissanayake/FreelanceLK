package lk.freelance.backend.service.impl;

import lk.freelance.backend.dto.*;
import lk.freelance.backend.entity.*;
import lk.freelance.backend.enums.*;
import lk.freelance.backend.exception.ResourceNotFoundException;
import lk.freelance.backend.exception.UnauthorizedException;
import lk.freelance.backend.repository.*;
import lk.freelance.backend.service.NotificationService;
import lk.freelance.backend.service.OrderService;
import lk.freelance.backend.service.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal PLATFORM_FEE_RATE = new BigDecimal("0.10");

    private final OrderRepository orderRepository;
    private final OrderMessageRepository orderMessageRepository;
    private final OrderDeliverableRepository orderDeliverableRepository;
    private final TransactionRepository transactionRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final DisputeRepository disputeRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;

    // CHANGE 1 & 4: Add the three new repositories
    private final GigRepository gigRepository;
    private final PartTimeJobRepository partTimeJobRepository;
    private final GigPackageRepository gigPackageRepository;

    // ================================================================
    // CREATE ORDER
    // ================================================================

    @Override
    @Transactional
    public OrderDetailDTO createOrder(UUID buyerId, CreateOrderRequest request) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        User seller = listing.getUser();

        if (seller.getUserId().equals(buyerId)) {
            throw new UnauthorizedException("You cannot place an order on your own listing");
        }

        // CHANGE 2: Real amount from package/gig/job
        BigDecimal amount = resolveOrderAmount(listing, request.getSelectedPackageId());
        BigDecimal platformFee = amount.multiply(PLATFORM_FEE_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = amount.add(platformFee);

        Wallet buyerWallet = walletRepository.findByUser_UserId(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer wallet not found"));

        if (buyerWallet.getBalance().compareTo(totalAmount) < 0) {
            throw new IllegalStateException("Insufficient wallet balance. Required: " + totalAmount
                    + ", Available: " + buyerWallet.getBalance());
        }

        buyerWallet.setBalance(buyerWallet.getBalance().subtract(totalAmount));
        walletRepository.save(buyerWallet);

        Wallet sellerWallet = walletRepository.findByUser_UserId(seller.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller wallet not found"));
        sellerWallet.setPendingBalance(sellerWallet.getPendingBalance().add(amount));
        walletRepository.save(sellerWallet);

        // CHANGE 3: Real delivery days
        int deliveryDays = resolveDeliveryDays(listing, request.getSelectedPackageId());

        String orderNumber = "ORD-" + System.currentTimeMillis();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .listing(listing)
                .buyer(buyer)
                .seller(seller)
                .amount(amount)
                .platformFee(platformFee)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.HELD)
                .deliveryDate(LocalDateTime.now().plusDays(deliveryDays))
                .build();

        order = orderRepository.save(order);

        Transaction tx = Transaction.builder()
                .order(order)
                .payer(buyer)
                .payee(seller)
                .amount(totalAmount)
                .transactionType("ESCROW")
                .status(PaymentStatus.HELD)
                .description("Escrow hold for order " + orderNumber)
                .build(); // createdAt set by @PrePersist
        transactionRepository.save(tx);

        if (request.getBuyerMessage() != null && !request.getBuyerMessage().isBlank()) {
            OrderMessage msg = OrderMessage.builder()
                    .order(order)
                    .sender(buyer)
                    .messageText(request.getBuyerMessage())
                    .isRead(false)
                    .build();
            orderMessageRepository.save(msg);
        }

        notificationService.send(seller, "ORDER_PLACED",
                "New order received",
                "You have a new order: " + listing.getTitle(),
                order.getOrderId());

        return orderMapper.toDetailDTO(order);
    }

    // ================================================================
    // GET ORDER
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderById(UUID userId, UUID orderId) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDTO getOrderByNumber(UUID userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));
        verifyOrderAccess(userId, order);
        return orderMapper.toDetailDTO(order);
    }

    // ================================================================
    // ORDER LIFECYCLE
    // ================================================================

    @Override
    @Transactional
    public OrderDetailDTO acceptOrder(UUID userId, UUID orderId) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        if (!order.getSeller().getUserId().equals(userId)) {
            throw new UnauthorizedException("Only the seller can accept this order");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING state");
        }
        order.setStatus(OrderStatus.ACCEPTED);
        order = orderRepository.save(order);
        notificationService.send(order.getBuyer(), "ORDER_ACCEPTED",
                "Order accepted",
                "Your order has been accepted: " + order.getListing().getTitle(),
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO startOrder(UUID userId, UUID orderId) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        if (!order.getSeller().getUserId().equals(userId)) {
            throw new UnauthorizedException("Only the seller can start this order");
        }
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Order must be ACCEPTED before starting");
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        order = orderRepository.save(order);
        notificationService.send(order.getBuyer(), "ORDER_STARTED",
                "Work started",
                "The seller has started working on: " + order.getListing().getTitle(),
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO submitDeliverable(UUID userId, UUID orderId, SubmitDeliverableRequest request) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        if (!order.getSeller().getUserId().equals(userId)) {
            throw new UnauthorizedException("Only the seller can submit deliverables");
        }
        if (order.getStatus() != OrderStatus.IN_PROGRESS && order.getStatus() != OrderStatus.REVISION) {
            throw new IllegalStateException("Order must be IN_PROGRESS or REVISION to submit a deliverable");
        }
        OrderDeliverable deliverable = OrderDeliverable.builder()
                .order(order)
                .fileUrl(request.getFileUrl())
                .fileName(request.getFileName())
                .fileSize(request.getFileSize())
                .description(request.getDescription())
                .deliveredAt(LocalDateTime.now())
                .build();
        orderDeliverableRepository.save(deliverable);
        order.setStatus(OrderStatus.DELIVERED);
        order = orderRepository.save(order);
        notificationService.send(order.getBuyer(), "DELIVERABLE_SUBMITTED",
                "Deliverable submitted",
                "The seller has submitted a deliverable for: " + order.getListing().getTitle(),
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO completeOrder(UUID buyerId, UUID orderId) {
        Order order = findOrderAndVerifyAccess(buyerId, orderId);
        if (!order.getBuyer().getUserId().equals(buyerId)) {
            throw new UnauthorizedException("Only the buyer can complete this order");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must be in DELIVERED state to complete");
        }

        Wallet sellerWallet = walletRepository.findByUser_UserId(order.getSeller().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller wallet not found"));
        sellerWallet.setPendingBalance(sellerWallet.getPendingBalance().subtract(order.getAmount()));
        sellerWallet.setBalance(sellerWallet.getBalance().add(order.getAmount()));
        walletRepository.save(sellerWallet);

        transactionRepository.findByOrder_OrderId(orderId).forEach(tx -> {
            tx.setStatus(PaymentStatus.RELEASED);
            tx.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(tx);
        });

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentStatus(PaymentStatus.RELEASED);
        order.setCompletedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        notificationService.send(order.getSeller(), "ORDER_COMPLETED",
                "Order completed",
                "Payment released for: " + order.getListing().getTitle(),
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO requestRevision(UUID userId, UUID orderId, String notes) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        if (!order.getBuyer().getUserId().equals(userId)) {
            throw new UnauthorizedException("Only the buyer can request revision");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Order must be DELIVERED to request revision");
        }
        order.setStatus(OrderStatus.REVISION);
        order = orderRepository.save(order);
        if (notes != null && !notes.isBlank()) {
            OrderMessage msg = OrderMessage.builder()
                    .order(order)
                    .sender(order.getBuyer())
                    .messageText("[Revision Request] " + notes)
                    .isRead(false)
                    .build();
            orderMessageRepository.save(msg);
        }
        notificationService.send(order.getSeller(), "REVISION_REQUESTED",
                "Revision requested",
                "The buyer has requested a revision for: " + order.getListing().getTitle(),
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    @Override
    @Transactional
    public OrderDetailDTO cancelOrder(UUID userId, UUID orderId, String reason) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        boolean isBuyer = order.getBuyer().getUserId().equals(userId);
        boolean isSeller = order.getSeller().getUserId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new UnauthorizedException("You are not part of this order");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order cannot be cancelled in its current state");
        }

        Wallet buyerWallet = walletRepository.findByUser_UserId(order.getBuyer().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Buyer wallet not found"));
        buyerWallet.setBalance(buyerWallet.getBalance().add(order.getTotalAmount()));
        walletRepository.save(buyerWallet);

        Wallet sellerWallet = walletRepository.findByUser_UserId(order.getSeller().getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller wallet not found"));
        if (sellerWallet.getPendingBalance().compareTo(order.getAmount()) >= 0) {
            sellerWallet.setPendingBalance(sellerWallet.getPendingBalance().subtract(order.getAmount()));
            walletRepository.save(sellerWallet);
        }

        transactionRepository.findByOrder_OrderId(orderId).forEach(tx -> {
            tx.setStatus(PaymentStatus.REFUNDED);
            tx.setProcessedAt(LocalDateTime.now());
            transactionRepository.save(tx);
        });

        order.setStatus(OrderStatus.CANCELLED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);
        order = orderRepository.save(order);

        User otherParty = isBuyer ? order.getSeller() : order.getBuyer();
        notificationService.send(otherParty, "ORDER_CANCELLED",
                "Order cancelled",
                "Order cancelled: " + order.getListing().getTitle() + ". Reason: " + reason,
                order.getOrderId());
        return orderMapper.toDetailDTO(order);
    }

    // ================================================================
    // PAGED LISTS
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryDTO> getBuyerOrders(UUID userId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPagedResponse(orderRepository.findByBuyer_UserId(userId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryDTO> getSellerOrders(UUID userId, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPagedResponse(orderRepository.findBySeller_UserId(userId, pageable));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryDTO> getAllOrders(UUID userId, String role, String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return toPagedResponse(orderRepository.findByBuyer_UserIdOrSeller_UserId(userId, userId, pageable));
    }

    // ================================================================
    // MESSAGES
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<OrderMessageDTO> getOrderMessages(UUID userId, UUID orderId) {
        findOrderAndVerifyAccess(userId, orderId);
        return orderMessageRepository.findByOrderOrderIdOrderByCreatedAtAsc(orderId)
                .stream().map(this::toMessageDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderMessageDTO sendOrderMessage(UUID userId, UUID orderId, SendOrderMessageRequest request) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        OrderMessage msg = OrderMessage.builder()
                .order(order)
                .sender(sender)
                .messageText(request.getMessageText())
                .attachmentUrl(request.getAttachmentUrl())
                .isRead(false)
                .build();
        msg = orderMessageRepository.save(msg);
        User recipient = order.getBuyer().getUserId().equals(userId) ? order.getSeller() : order.getBuyer();
        String senderName = sender.getUserProfile() != null
                ? sender.getUserProfile().getDisplayName() : sender.getEmail();
        notificationService.send(recipient, "ORDER_MESSAGE",
                "New message",
                senderName + " sent a message on: " + order.getListing().getTitle(),
                orderId);
        return toMessageDTO(msg);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(UUID userId, UUID orderId) {
        findOrderAndVerifyAccess(userId, orderId);
        orderMessageRepository.findByOrderOrderIdAndIsRead(orderId, false).forEach(msg -> {
            if (!msg.getSender().getUserId().equals(userId)) {
                msg.setIsRead(true);
                orderMessageRepository.save(msg);
            }
        });
    }

    // ================================================================
    // DELIVERABLES
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<DeliverableDTO> getOrderDeliverables(UUID userId, UUID orderId) {
        findOrderAndVerifyAccess(userId, orderId);
        return orderDeliverableRepository.findByOrder_OrderId(orderId).stream()
                .map(d -> DeliverableDTO.builder()
                        .deliverableId(d.getDeliverableId())
                        .fileUrl(d.getFileUrl())
                        .fileName(d.getFileName())
                        .fileSize(d.getFileSize())
                        .description(d.getDescription())
                        .deliveredAt(d.getDeliveredAt())
                        .createdAt(d.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadUrlDTO generateDeliverableDownloadUrl(UUID userId, UUID orderId, UUID deliverableId) {
        findOrderAndVerifyAccess(userId, orderId);
        OrderDeliverable deliverable = orderDeliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverable not found"));
        if (!deliverable.getOrder().getOrderId().equals(orderId)) {
            throw new UnauthorizedException("Deliverable does not belong to this order");
        }
        return DownloadUrlDTO.builder()
                .downloadUrl(deliverable.getFileUrl())
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    // ================================================================
    // DISPUTES
    // ================================================================

    @Override
    @Transactional
    public DisputeDTO raiseDispute(UUID userId, UUID orderId, RaiseDisputeRequest request) {
        Order order = findOrderAndVerifyAccess(userId, orderId);
        if (disputeRepository.findByOrder_OrderId(orderId).isPresent()) {
            throw new IllegalStateException("A dispute already exists for this order");
        }
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot raise a dispute on a completed or cancelled order");
        }
        User raisedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Dispute dispute = Dispute.builder()
                .order(order)
                .raisedBy(raisedBy)
                .reason(request.getReason())
                .description(request.getDescription())
                .status("OPEN")
                .build();
        dispute = disputeRepository.save(dispute);
        order.setStatus(OrderStatus.DISPUTED);
        orderRepository.save(order);
        User otherParty = order.getBuyer().getUserId().equals(userId) ? order.getSeller() : order.getBuyer();
        notificationService.send(otherParty, "DISPUTE_RAISED",
                "Dispute raised",
                "A dispute has been raised for: " + order.getListing().getTitle(),
                orderId);
        return toDisputeDTO(dispute);
    }

    @Override
    @Transactional(readOnly = true)
    public DisputeDTO getOrderDispute(UUID userId, UUID orderId) {
        findOrderAndVerifyAccess(userId, orderId);
        Dispute dispute = disputeRepository.findByOrder_OrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("No dispute found for this order"));
        return toDisputeDTO(dispute);
    }

    // ================================================================
    // STATISTICS
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsDTO getOrderStatistics(UUID userId) {
        List<Order> asBuyer = orderRepository.findByBuyer_UserId(userId, Pageable.unpaged()).getContent();
        List<Order> asSeller = orderRepository.findBySeller_UserId(userId);

        long completedTotal = asBuyer.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count()
                + asSeller.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
        long cancelledTotal = asBuyer.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count()
                + asSeller.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        long activeTotal = asBuyer.stream().filter(o -> isActiveStatus(o.getStatus())).count()
                + asSeller.stream().filter(o -> isActiveStatus(o.getStatus())).count();

        BigDecimal totalSpent = asBuyer.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalEarned = asSeller.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalOrders = asBuyer.size() + asSeller.size();
        BigDecimal avgOrderValue = totalOrders > 0
                ? totalSpent.add(totalEarned).divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal completionRate = totalOrders > 0
                ? BigDecimal.valueOf(completedTotal)
                .divide(BigDecimal.valueOf(totalOrders), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return OrderStatisticsDTO.builder()
                .totalOrders(totalOrders)
                .activeOrders((int) activeTotal)
                .completedOrders((int) completedTotal)
                .cancelledOrders((int) cancelledTotal)
                .totalSpent(totalSpent)
                .totalEarned(totalEarned)
                .averageOrderValue(avgOrderValue)
                .completionRate(completionRate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCountDTO getActiveOrdersCount(UUID userId) {
        int activeBuyer = (int) orderRepository.findByBuyer_UserId(userId, Pageable.unpaged())
                .getContent().stream().filter(o -> isActiveStatus(o.getStatus())).count();
        int activeSeller = (int) orderRepository.findBySeller_UserId(userId)
                .stream().filter(o -> isActiveStatus(o.getStatus())).count();
        return OrderCountDTO.builder()
                .asBuyer(activeBuyer)
                .asSeller(activeSeller)
                .total(activeBuyer + activeSeller)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSummaryDTO> getPendingActions(UUID userId) {
        List<OrderSummaryDTO> result = new ArrayList<>();
        orderRepository.findByBuyer_UserId(userId, Pageable.unpaged()).getContent().stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(orderMapper::toSummaryDTO).forEach(result::add);
        orderRepository.findBySeller_UserId(userId).stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING || o.getStatus() == OrderStatus.REVISION)
                .map(orderMapper::toSummaryDTO).forEach(result::add);
        return result;
    }

    // ================================================================
    // NEW PRIVATE HELPERS (from Claude)
    // ================================================================

    private BigDecimal resolveOrderAmount(Listing listing, UUID selectedPackageId) {
        if (listing.getListingType() == ListingType.GIG) {
            if (selectedPackageId != null) {
                GigPackage pkg = gigPackageRepository.findById(selectedPackageId)
                        .orElseThrow(() -> new ResourceNotFoundException("GigPackage", "id", selectedPackageId));
                if (!pkg.getGig().getListing().getListingId().equals(listing.getListingId())) {
                    throw new IllegalArgumentException("Selected package does not belong to this listing");
                }
                return pkg.getPrice();
            }
            return gigRepository.findByListing_ListingId(listing.getListingId())
                    .map(Gig::getBasePrice)
                    .orElseThrow(() -> new ResourceNotFoundException("Gig not found for listing: " + listing.getListingId()));
        }

        return partTimeJobRepository.findByListing_ListingId(listing.getListingId())
                .map(job -> job.getHourlyRate() != null ? job.getHourlyRate() : job.getMonthlySalary())
                .orElseThrow(() -> new ResourceNotFoundException("Job not found for listing: " + listing.getListingId()));
    }

    private int resolveDeliveryDays(Listing listing, UUID selectedPackageId) {
        if (listing.getListingType() == ListingType.GIG) {
            if (selectedPackageId != null) {
                return gigPackageRepository.findById(selectedPackageId)
                        .map(GigPackage::getDeliveryDays)
                        .orElse(7);
            }
            return gigRepository.findByListing_ListingId(listing.getListingId())
                    .map(Gig::getDeliveryDays)
                    .orElse(7);
        }
        return partTimeJobRepository.findByListing_ListingId(listing.getListingId())
                .map(job -> job.getDurationMonths() != null ? job.getDurationMonths() * 30 : 30)
                .orElse(30);
    }

    // ================================================================
    // EXISTING PRIVATE HELPERS (unchanged)
    // ================================================================

    private Order findOrderAndVerifyAccess(UUID userId, UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        verifyOrderAccess(userId, order);
        return order;
    }

    private void verifyOrderAccess(UUID userId, Order order) {
        if (!order.getBuyer().getUserId().equals(userId) && !order.getSeller().getUserId().equals(userId)) {
            throw new UnauthorizedException("You do not have access to this order");
        }
    }

    private boolean isActiveStatus(OrderStatus status) {
        return status == OrderStatus.PENDING || status == OrderStatus.ACCEPTED
                || status == OrderStatus.IN_PROGRESS || status == OrderStatus.DELIVERED
                || status == OrderStatus.REVISION;
    }

    private PagedResponse<OrderSummaryDTO> toPagedResponse(Page<Order> page) {
        return PagedResponse.<OrderSummaryDTO>builder()
                .content(page.getContent().stream().map(orderMapper::toSummaryDTO).collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .pageSize(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private OrderMessageDTO toMessageDTO(OrderMessage msg) {
        return OrderMessageDTO.builder()
                .messageId(msg.getMessageId())
                .senderId(msg.getSender().getUserId())
                .senderName(msg.getSender().getUserProfile() != null
                        ? msg.getSender().getUserProfile().getDisplayName()
                        : msg.getSender().getEmail())
                .messageText(msg.getMessageText())
                .attachmentUrl(msg.getAttachmentUrl())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }

    private DisputeDTO toDisputeDTO(Dispute d) {
        return DisputeDTO.builder()
                .disputeId(d.getDisputeId())
                .orderId(d.getOrder().getOrderId())
                .orderNumber(d.getOrder().getOrderNumber())
                .raisedBy(d.getRaisedBy().getUserId())
                .raisedByName(d.getRaisedBy().getUserProfile() != null
                        ? d.getRaisedBy().getUserProfile().getDisplayName()
                        : d.getRaisedBy().getEmail())
                .reason(d.getReason())
                .description(d.getDescription())
                .status(d.getStatus())
                .resolution(d.getResolution())
                .createdAt(d.getCreatedAt())
                .resolvedAt(d.getResolvedAt())
                .build();
    }
}