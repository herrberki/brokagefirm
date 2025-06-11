package com.myproject.brokagefirmchallenge.repo.observer;


import lombok.Getter;

@Getter
public class OrderCanceledEvent extends OrderEvent {

    private final String cancelReason;

    public OrderCanceledEvent(Object source, Long orderId, Long customerId,
                              String assetName, String cancelReason) {
        super(source, orderId, customerId, assetName);
        this.cancelReason = cancelReason;
    }

    @Override
    public String getEventType() {
        return "ORDER_CANCELED";
    }
}
