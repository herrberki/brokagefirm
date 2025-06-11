package com.myproject.brokagefirmchallenge.repo.service;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataService {

    private final Map<String, BigDecimal> marketPrices = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public MarketDataService() {
        initializeMarketPrices();
    }

    private void initializeMarketPrices() {
        marketPrices.put("AAPL", new BigDecimal("150.00"));
        marketPrices.put("GOOGL", new BigDecimal("2800.00"));
        marketPrices.put("MSFT", new BigDecimal("300.00"));
        marketPrices.put("AMZN", new BigDecimal("3200.00"));
        marketPrices.put("TSLA", new BigDecimal("800.00"));
        marketPrices.put("BTC", new BigDecimal("45000.00"));
        marketPrices.put("ETH", new BigDecimal("3000.00"));
    }

    @Cacheable(value = "marketDataCache", key = "#assetName")
    public BigDecimal getCurrentPrice(String assetName) {
        BigDecimal basePrice = marketPrices.get(assetName);

        if (basePrice == null) {
            basePrice = generateRandomPrice();
            marketPrices.put(assetName, basePrice);
        }

        double volatility = 0.02;
        double change = (random.nextDouble() - 0.5) * 2 * volatility;

        BigDecimal currentPrice = basePrice.multiply(BigDecimal.valueOf(1 + change));
        return currentPrice.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal generateRandomPrice() {
        double price = 10 + (random.nextDouble() * 990);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }
}