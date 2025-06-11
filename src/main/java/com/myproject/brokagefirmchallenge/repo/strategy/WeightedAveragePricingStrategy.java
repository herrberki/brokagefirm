package com.myproject.brokagefirmchallenge.repo.strategy;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("weightedAveragePricingStrategy")
public class WeightedAveragePricingStrategy implements PricingStrategy {

    @Override
    public BigDecimal calculateExecutionPrice(Order buyOrder, Order sellOrder) {
        BigDecimal buyWeight = buyOrder.getRemainingSize();
        BigDecimal sellWeight = sellOrder.getRemainingSize();
        BigDecimal totalWeight = buyWeight.add(sellWeight);

        BigDecimal weightedBuyPrice = buyOrder.getPrice().multiply(buyWeight);
        BigDecimal weightedSellPrice = sellOrder.getPrice().multiply(sellWeight);

        return weightedBuyPrice.add(weightedSellPrice)
                .divide(totalWeight, 2, RoundingMode.HALF_UP);
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
