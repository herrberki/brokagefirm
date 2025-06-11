package com.myproject.brokagefirmchallenge.repo.strategy;

import com.myproject.brokagefirmchallenge.repo.entity.Order;

import java.math.BigDecimal;

public interface PricingStrategy {

    BigDecimal calculateExecutionPrice(Order buyOrder, Order sellOrder);

    boolean canMatch(Order buyOrder, Order sellOrder);
}
