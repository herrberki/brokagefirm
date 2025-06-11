package com.myproject.brokagefirmchallenge.repo.observer;

import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderCreatedEvent extends OrderEvent {

    private final OrderSide side;
    private final BigDecimal size;
    private final BigDecimal price;

    public OrderCreatedEvent(Object source, Long orderId, Long customerId,
                             String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        super(source, orderId, customerId, assetName);
        this.side = side;
        this.size = size;
        this.price = price;
    }

    @Override
    public String getEventType() {
        return "ORDER_CREATED";
    }
}
