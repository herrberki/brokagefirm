package com.myproject.brokagefirmchallenge.repo.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PricingStrategyFactory {

    private final Map<String, PricingStrategy> strategies;

    public PricingStrategy getStrategy(String strategyName) {
        PricingStrategy strategy = strategies.get(strategyName + "PricingStrategy");

        if (strategy == null) {
            return strategies.get("takerPricingStrategy");
        }

        return strategy;
    }
}
