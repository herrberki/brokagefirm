package com.myproject.brokagefirmchallenge.repo.factory;

import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderFactoryProvider {

    private final List<OrderFactory> orderFactories;

    public OrderFactory getFactory(CreateOrderRequest request) {
        return orderFactories.stream()
                .filter(factory -> factory.supports(request))
                .findFirst()
                .orElseThrow(() -> new InvalidOrderException("No suitable factory found for order type"));
    }
}
