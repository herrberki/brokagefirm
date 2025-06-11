package com.myproject.brokagefirmchallenge.repo.observer;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderMatchedEvent extends OrderEvent {

    private final Long matchingOrderId;
    private final BigDecimal executedSize;
    private final BigDecimal executionPrice;
    private final boolean fullyMatched;

    public OrderMatchedEvent(Object source, Long orderId, Long customerId, String assetName,
                             Long matchingOrderId, BigDecimal executedSize,
                             BigDecimal executionPrice, boolean fullyMatched) {
        super(source, orderId, customerId, assetName);
        this.matchingOrderId = matchingOrderId;
        this.executedSize = executedSize;
        this.executionPrice = executionPrice;
        this.fullyMatched = fullyMatched;
    }
}
