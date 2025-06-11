package com.myproject.brokagefirmchallenge.repo.factory;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class LimitOrderFactory extends AbstractOrderFactory {

    private static final BigDecimal MIN_ORDER_SIZE = new BigDecimal("0.0001");
    private static final BigDecimal MAX_ORDER_SIZE = new BigDecimal("1000000");
    private static final BigDecimal MIN_ORDER_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_ORDER_PRICE = new BigDecimal("999999");

    @Override
    public boolean supports(CreateOrderRequest request) {
        return request.getPrice() != null &&
                request.getPrice().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    protected void validateRequest(CreateOrderRequest request) {
        validateCommonFields(request);

        if (request.getSize().compareTo(MIN_ORDER_SIZE) < 0) {
            throw new InvalidOrderException("Order size must be at least " + MIN_ORDER_SIZE);
        }

        if (request.getSize().compareTo(MAX_ORDER_SIZE) > 0) {
            throw new InvalidOrderException("Order size cannot exceed " + MAX_ORDER_SIZE);
        }

        if (request.getPrice().compareTo(MIN_ORDER_PRICE) < 0) {
            throw new InvalidOrderException("Order price must be at least " + MIN_ORDER_PRICE);
        }

        if (request.getPrice().compareTo(MAX_ORDER_PRICE) > 0) {
            throw new InvalidOrderException("Order price cannot exceed " + MAX_ORDER_PRICE);
        }
    }

    @Override
    protected void enrichOrder(Order order, CreateOrderRequest request) {
    }
}
