package com.myproject.brokagefirmchallenge.repo.service.impl;


import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    @Override
    public void notifyOrderCreated(Long customerId, Long orderId, String assetName,
                                   OrderSide side, BigDecimal size, BigDecimal price) {
        log.info("Notification: Order {} created for customer {} - {} {} {} @ {}",
                orderId, customerId, side, size, assetName, price);
    }

    @Override
    public void notifyOrderMatched(Long customerId, Long orderId,
                                   BigDecimal executedSize, BigDecimal executionPrice, boolean fullyMatched) {
        String status = fullyMatched ? "fully matched" : "partially matched";
        log.info("Notification: Order {} {} for customer {} - {} @ {}",
                orderId, status, customerId, executedSize, executionPrice);
    }

    @Override
    public void notifyOrderCanceled(Long customerId, Long orderId, String reason) {
        log.info("Notification: Order {} canceled for customer {} - Reason: {}",
                orderId, customerId, reason);
    }

    @Override
    public void notifySignificantBalanceChange(Long customerId, String assetName,
                                               BigDecimal oldBalance, BigDecimal newBalance) {
        log.info("Notification: Significant balance change for customer {} - {} balance changed from {} to {}",
                customerId, assetName, oldBalance, newBalance);
    }
}
