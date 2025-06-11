package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.*;
import com.myproject.brokagefirmchallenge.repo.publisher.EventPublisher;
import com.myproject.brokagefirmchallenge.repo.repository.OrderRepository;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private AuditService auditService;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private OrderServiceImpl orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(10L);
        order.setCustomerId(1L);
        order.setAssetName("BTC");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(new BigDecimal("1"));
        order.setPrice(new BigDecimal("100"));
        order.setStatus(OrderStatus.PENDING);
        order.setRemainingSize(order.getSize());
    }

    @Test
    @DisplayName("should_createOrder_invalidSize_throwInvalidOrderException")
    void should_createOrder_invalidSize_throw() {
        // given
        order.setSize(new BigDecimal("0.00001"));
        // when // then
        assertThatThrownBy(() -> orderService.createOrder(order))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("Order size must be at least");
    }

    @Test
    @DisplayName("should_cancelOrder_success_inOrder")
    void should_cancelOrder_success_inOrder() {
        // given
        order.setOrderSide(OrderSide.SELL);
        order.setStatus(OrderStatus.PENDING);
        order.setRemainingSize(new BigDecimal("2"));
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        // when
        Order result = orderService.cancelOrder(10L, 1L);
        // then
        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELED);
        InOrder in = inOrder(orderRepository, auditService, eventPublisher);
        in.verify(orderRepository).findByIdForUpdate(10L);
        in.verify(orderRepository).save(order);
        in.verify(auditService).auditOrderAction(
                10L, AuditAction.ORDER_CANCELED, "Order canceled by user");
        in.verify(eventPublisher).publishOrderCanceled(order, "Canceled by user");
        in.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("should_cancelOrder_unauthorized_throwUnauthorizedAccessException")
    void should_cancelOrder_unauthorized_throw() {
        // given
        order.setCustomerId(2L);
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        // when // then
        assertThatThrownBy(() -> orderService.cancelOrder(10L, 1L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    @DisplayName("should_cancelOrder_invalidState_throwInvalidOrderStateException")
    void should_cancelOrder_invalidState_throw() {
        // given
        order.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(order));
        // when // then
        assertThatThrownBy(() -> orderService.cancelOrder(10L, 1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("should_calculateOrderValue_and_isOrderCancelable")
    void should_calculate_and_isCancelable() {
        // given
        BigDecimal value = new BigDecimal("2").multiply(new BigDecimal("50"));
        // when
        BigDecimal result = orderService.calculateOrderValue(new BigDecimal("2"), new BigDecimal("50"));
        boolean cancelable1 = orderService.isOrderCancelable(order);
        order.setStatus(OrderStatus.PARTIALLY_MATCHED);
        boolean cancelable2 = orderService.isOrderCancelable(order);
        order.setStatus(OrderStatus.MATCHED);
        boolean cancelable3 = orderService.isOrderCancelable(order);
        // then
        assertThat(result).isEqualByComparingTo(value);
        assertThat(cancelable1).isTrue();
        assertThat(cancelable2).isTrue();
        assertThat(cancelable3).isFalse();
    }
}
