package com.myproject.brokagefirmchallenge.repo.publisher;


import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.observer.OrderCanceledEvent;
import com.myproject.brokagefirmchallenge.repo.observer.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishOrderCreated(Order order) {
        OrderCreatedEvent event = new OrderCreatedEvent(
                this,
                order.getId(),
                order.getCustomerId(),
                order.getAssetName(),
                order.getOrderSide(),
                order.getSize(),
                order.getPrice()
        );

        log.debug("Publishing order created event: {}", event.getOrderId());
        applicationEventPublisher.publishEvent(event);
    }


    public void publishOrderCanceled(Order order, String reason) {
        OrderCanceledEvent event = new OrderCanceledEvent(
                this,
                order.getId(),
                order.getCustomerId(),
                order.getAssetName(),
                reason
        );

        log.debug("Publishing order canceled event: {}", event.getOrderId());
        applicationEventPublisher.publishEvent(event);
    }
}
