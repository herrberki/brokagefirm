package com.myproject.brokagefirmchallenge.repo.service;


import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;

import java.math.BigDecimal;

public interface NotificationService {

    void notifyOrderCreated(Long customerId, Long orderId, String assetName,
                            OrderSide side, BigDecimal size, BigDecimal price);

    void notifyOrderMatched(Long customerId, Long orderId,
                            BigDecimal executedSize, BigDecimal executionPrice, boolean fullyMatched);

    void notifyOrderCanceled(Long customerId, Long orderId, String reason);

    void notifySignificantBalanceChange(Long customerId, String assetName,
                                        BigDecimal oldBalance, BigDecimal newBalance);
}
