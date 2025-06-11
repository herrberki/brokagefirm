package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.entity.Order;

import java.math.BigDecimal;

public interface OrderMatchingService {

    void matchOrders(String assetName);

    void matchAllPendingOrders();

    boolean canMatch(Order buyOrder, Order sellOrder);

    BigDecimal calculateMatchSize(Order buyOrder, Order sellOrder);

    BigDecimal calculateMatchPrice(Order buyOrder, Order sellOrder);

    void addOrderToBook(Order order);
}
