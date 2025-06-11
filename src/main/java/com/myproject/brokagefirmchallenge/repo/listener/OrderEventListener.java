package com.myproject.brokagefirmchallenge.repo.listener;

import com.myproject.brokagefirmchallenge.repo.observer.BalanceUpdateEvent;
import com.myproject.brokagefirmchallenge.repo.observer.OrderCanceledEvent;
import com.myproject.brokagefirmchallenge.repo.observer.OrderCreatedEvent;
import com.myproject.brokagefirmchallenge.repo.observer.OrderMatchedEvent;
import com.myproject.brokagefirmchallenge.repo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order created event received: {}", event.getOrderId());

        notificationService.notifyOrderCreated(
                event.getCustomerId(),
                event.getOrderId(),
                event.getAssetName(),
                event.getSide(),
                event.getSize(),
                event.getPrice()
        );
    }

    @TransactionalEventListener
    public void handleOrderMatched(OrderMatchedEvent event) {
        log.info("Order matched event received: {} with {}",
                event.getOrderId(), event.getMatchingOrderId());

        notificationService.notifyOrderMatched(
                event.getCustomerId(),
                event.getOrderId(),
                event.getExecutedSize(),
                event.getExecutionPrice(),
                event.isFullyMatched()
        );
    }

    @Async
    @EventListener
    public void handleOrderCanceled(OrderCanceledEvent event) {
        log.info("Order canceled event received: {}", event.getOrderId());

        notificationService.notifyOrderCanceled(
                event.getCustomerId(),
                event.getOrderId(),
                event.getCancelReason()
        );
    }

    @Async
    @EventListener
    public void handleBalanceUpdate(BalanceUpdateEvent event) {
        log.info("Balance update event received for customer: {} asset: {}",
                event.getCustomerId(), event.getAssetName());

        BigDecimal change = event.getNewBalance().subtract(event.getOldBalance()).abs();
        BigDecimal threshold = event.getOldBalance().multiply(BigDecimal.valueOf(0.1));

        if (change.compareTo(threshold) > 0) {
            notificationService.notifySignificantBalanceChange(
                    event.getCustomerId(),
                    event.getAssetName(),
                    event.getOldBalance(),
                    event.getNewBalance()
            );
        }
    }
}
