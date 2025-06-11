package com.myproject.brokagefirmchallenge.repo.observer;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public abstract class OrderEvent extends ApplicationEvent {

    private final Long orderId;
    private final Long customerId;
    private final String assetName;
    private final LocalDateTime occurredAt;

    protected OrderEvent(Object source, Long orderId, Long customerId, String assetName) {
        super(source);
        this.orderId = orderId;
        this.customerId = customerId;
        this.assetName = assetName;
        this.occurredAt = LocalDateTime.now();
    }
}