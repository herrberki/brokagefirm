package com.myproject.brokagefirmchallenge.repo.factory;


import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;

import java.math.BigDecimal;

public abstract class AbstractOrderFactory implements OrderFactory {

    @Override
    public Order createOrder(CreateOrderRequest request) {
        validateRequest(request);

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .assetName(request.getAssetName())
                .orderSide(request.getSide())
                .size(request.getSize())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .executedSize(BigDecimal.ZERO)
                .remainingSize(request.getSize())
                .totalAmount(calculateTotalAmount(request))
                .build();

        enrichOrder(order, request);

        return order;
    }

    protected abstract void validateRequest(CreateOrderRequest request);

    protected abstract void enrichOrder(Order order, CreateOrderRequest request);

    protected BigDecimal calculateTotalAmount(CreateOrderRequest request) {
        return request.getSize().multiply(request.getPrice());
    }

    protected void validateCommonFields(CreateOrderRequest request) {
        if (request.getSize().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order size must be positive");
        }

        if (request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderException("Order price must be positive");
        }
    }
}
