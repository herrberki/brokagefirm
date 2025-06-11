package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.slf4j.Logger;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

class NotificationServiceImplTest {

    private static Logger mockLogger;
    private final NotificationServiceImpl service = new NotificationServiceImpl();

    @BeforeAll
    static void injectLogger() {
        mockLogger = Mockito.mock(Logger.class);
        ReflectionTestUtils.setField(NotificationServiceImpl.class, "log", mockLogger);
    }

    @Test
    @DisplayName("notifyOrderCreated logs in correct order")
    void notifyOrderCreated_logsInOrder() {
        Long customerId = 10L;
        Long orderId = 20L;
        String asset = "BTC";
        OrderSide side = OrderSide.BUY;
        BigDecimal size = new BigDecimal("1.5");
        BigDecimal price = new BigDecimal("50000");

        service.notifyOrderCreated(customerId, orderId, asset, side, size, price);

        InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).info(
                "Notification: Order {} created for customer {} - {} {} {} @ {}",
                orderId, customerId, side, size, asset, price
        );
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("notifyOrderMatched logs in correct order")
    void notifyOrderMatched_logsInOrder() {
        Long customerId = 11L;
        Long orderId = 21L;
        BigDecimal executedSize = new BigDecimal("2.0");
        BigDecimal executionPrice = new BigDecimal("100");
        boolean fully = true;

        service.notifyOrderMatched(customerId, orderId, executedSize, executionPrice, fully);

        InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).info(
                "Notification: Order {} {} for customer {} - {} @ {}",
                orderId, "fully matched", customerId, executedSize, executionPrice
        );
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("notifyOrderCanceled logs in correct order")
    void notifyOrderCanceled_logsInOrder() {
        Long customerId = 12L;
        Long orderId = 22L;
        String reason = "timeout";

        service.notifyOrderCanceled(customerId, orderId, reason);

        InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).info(
                "Notification: Order {} canceled for customer {} - Reason: {}",
                orderId, customerId, reason
        );
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("notifySignificantBalanceChange logs in correct order")
    void notifySignificantBalanceChange_logsInOrder() {
        Long customerId = 13L;
        String asset = "USD";
        BigDecimal oldBal = new BigDecimal("100");
        BigDecimal newBal = new BigDecimal("150");

        service.notifySignificantBalanceChange(customerId, asset, oldBal, newBal);

        InOrder inOrder = inOrder(mockLogger);
        inOrder.verify(mockLogger).info(
                "Notification: Significant balance change for customer {} - {} balance changed from {} to {}",
                customerId, asset, oldBal, newBal
        );
        inOrder.verifyNoMoreInteractions();
    }
}
