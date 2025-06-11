package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderService {

    Order createOrder(Order order);

    Order cancelOrder(Long orderId, Long customerId);

    Optional<Order> findById(Long orderId);

    Page<Order> findOrders(Specification<Order> specification, Pageable pageable);

    BigDecimal calculateOrderValue(BigDecimal size, BigDecimal price);

    boolean isOrderCancelable(Order order);

    void validateOrderCreation(Order order);
}
