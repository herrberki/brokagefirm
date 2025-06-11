package com.myproject.brokagefirmchallenge.repo.strategy;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("takerPricingStrategy")
public class TakerPricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculateExecutionPrice(Order buyOrder, Order sellOrder) {
        return sellOrder.getPrice();
    }

    @Override
    public boolean canMatch(Order buyOrder, Order sellOrder) {
        return buyOrder.getPrice().compareTo(sellOrder.getPrice()) >= 0 &&
                buyOrder.getRemainingSize().compareTo(BigDecimal.ZERO) > 0 &&
                sellOrder.getRemainingSize().compareTo(BigDecimal.ZERO) > 0 &&
                buyOrder.getAssetName().equals(sellOrder.getAssetName()) &&
                !buyOrder.getCustomerId().equals(sellOrder.getCustomerId());
    }
}
