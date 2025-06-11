package com.myproject.brokagefirmchallenge.repo.factory;


import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;


public interface OrderFactory {

    Order createOrder(CreateOrderRequest request);

    boolean supports(CreateOrderRequest request);
}
