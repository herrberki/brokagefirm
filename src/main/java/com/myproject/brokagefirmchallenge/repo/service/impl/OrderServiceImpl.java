package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.*;
import com.myproject.brokagefirmchallenge.repo.publisher.EventPublisher;
import com.myproject.brokagefirmchallenge.repo.repository.OrderRepository;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import com.myproject.brokagefirmchallenge.repo.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final AssetService assetService;
    private final AuditService auditService;
    private final EventPublisher eventPublisher;

    private static final String TRY_ASSET = "TRY";
    private static final BigDecimal MIN_ORDER_SIZE = new BigDecimal("0.0001");
    private static final BigDecimal MIN_ORDER_PRICE = new BigDecimal("0.01");

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Order createOrder(Order order) {
        log.info("Creating order for customer: {}, asset: {}, side: {}, size: {}, price: {}",
                order.getCustomerId(), order.getAssetName(), order.getOrderSide(),
                order.getSize(), order.getPrice());

        validateOrderCreation(order);
        blockOrderAssets(order);
        prepareNewOrder(order);
        Order savedOrder = orderRepository.save(order);
        auditOrderPlacement(savedOrder);
        eventPublisher.publishOrderCreated(savedOrder);

        log.info("Order created successfully with ID: {}", savedOrder.getId());
        return savedOrder;
    }

    private void blockOrderAssets(Order order) {
        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal requiredTry = calculateOrderValue(order.getSize(), order.getPrice());
            assetService.blockAsset(order.getCustomerId(), TRY_ASSET, requiredTry);
        } else {
            assetService.blockAsset(order.getCustomerId(), order.getAssetName(), order.getSize());
        }
    }

    private void prepareNewOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        order.setExecutedSize(BigDecimal.ZERO);
        order.setRemainingSize(order.getSize());
        order.setTotalAmount(calculateOrderValue(order.getSize(), order.getPrice()));
    }

    private void auditOrderPlacement(Order order) {
        auditService.auditOrderAction(order.getId(), AuditAction.ORDER_PLACED,
                String.format("Order placed - %s %s %s @ %s",
                        order.getOrderSide(), order.getSize(), order.getAssetName(), order.getPrice()));
    }


    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Order cancelOrder(Long orderId, Long customerId) {
        log.info("Canceling order ID: {} for customer: {}", orderId, customerId);

        Order order = getOrderForUpdateOrThrow(orderId);

        validateOrderCancelAuthorization(order, customerId);
        validateOrderIsCancelable(order);

        releaseOrderAssets(order);

        order.setStatus(OrderStatus.CANCELED);
        order.setCancelReason("Canceled by user");
        Order canceledOrder = orderRepository.save(order);

        auditOrderCancellation(orderId);
        eventPublisher.publishOrderCanceled(canceledOrder, "Canceled by user");
        log.info("Order canceled successfully: {}", orderId);

        return canceledOrder;
    }

    private Order getOrderForUpdateOrThrow(Long orderId) {
        return orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    private void validateOrderCancelAuthorization(Order order, Long customerId) {
        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("Order does not belong to customer: " + customerId);
        }
    }

    private void validateOrderIsCancelable(Order order) {
        if (!isOrderCancelable(order)) {
            throw new InvalidOrderStateException("Order cannot be canceled in status: " + order.getStatus());
        }
    }

    private void releaseOrderAssets(Order order) {
        BigDecimal amountToRelease = order.getRemainingSize();
        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal tryToRelease = calculateOrderValue(amountToRelease, order.getPrice());
            assetService.releaseAsset(order.getCustomerId(), TRY_ASSET, tryToRelease);
        } else {
            assetService.releaseAsset(order.getCustomerId(), order.getAssetName(), amountToRelease);
        }
    }

    private void auditOrderCancellation(Long orderId) {
        auditService.auditOrderAction(orderId, AuditAction.ORDER_CANCELED, "Order canceled by user");
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Order> findOrders(Specification<Order> specification, Pageable pageable) {
        return orderRepository.findAll(specification, pageable);
    }

    @Override
    public BigDecimal calculateOrderValue(BigDecimal size, BigDecimal price) {
        return size.multiply(price);
    }

    @Override
    public boolean isOrderCancelable(Order order) {
        return order.getStatus() == OrderStatus.PENDING ||
                order.getStatus() == OrderStatus.PARTIALLY_MATCHED;
    }

    @Override
    public void validateOrderCreation(Order order) {
        validateOrderSize(order.getSize());
        validateOrderPrice(order.getPrice());
        validateAssetName(order.getAssetName());

        AssetRequirement req = getAssetRequirement(order);

        validateCustomerBalance(order.getCustomerId(), req.asset(), req.requiredAmount());
    }

    private void validateOrderSize(BigDecimal size) {
        if (size.compareTo(MIN_ORDER_SIZE) < 0)
            throw new InvalidOrderException("Order size must be at least " + MIN_ORDER_SIZE);
    }

    private void validateOrderPrice(BigDecimal price) {
        if (price.compareTo(MIN_ORDER_PRICE) < 0)
            throw new InvalidOrderException("Order price must be at least " + MIN_ORDER_PRICE);
    }

    private void validateAssetName(String assetName) {
        if (TRY_ASSET.equals(assetName))
            throw new InvalidOrderException("Cannot trade TRY against TRY");
    }

    private AssetRequirement getAssetRequirement(Order order) {
        return (order.getOrderSide() == OrderSide.BUY)
                ? new AssetRequirement(TRY_ASSET, calculateOrderValue(order.getSize(), order.getPrice()))
                : new AssetRequirement(order.getAssetName(), order.getSize());
    }

    private void validateCustomerBalance(Long customerId, String asset, BigDecimal amount) {
        if (!assetService.isEnoughBalance(customerId, asset, amount)) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient %s balance. Required: %s", asset, amount));
        }
    }

    private record AssetRequirement(String asset, BigDecimal requiredAmount) {}

}
