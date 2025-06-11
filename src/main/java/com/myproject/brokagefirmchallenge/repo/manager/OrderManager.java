package com.myproject.brokagefirmchallenge.repo.manager;

import com.myproject.brokagefirmchallenge.repo.converter.OrderMapper;
import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.factory.OrderFactory;
import com.myproject.brokagefirmchallenge.repo.factory.OrderFactoryProvider;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.request.ListOrdersRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.response.PagedResponse;
import com.myproject.brokagefirmchallenge.repo.security.SecurityContextManager;
import com.myproject.brokagefirmchallenge.repo.service.OrderService;
import com.myproject.brokagefirmchallenge.repo.specifications.OrderSpecifications;
import com.myproject.brokagefirmchallenge.repo.validator.OrderValidator;
import com.myproject.brokagefirmchallenge.repo.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderManager {

    private final OrderMapper orderMapper;
    private final OrderService orderService;
    private final SecurityContextManager securityContextManager;
    private final OrderValidator orderValidator;
    private final OrderFactoryProvider orderFactoryProvider;


    @Transactional
    public ApiResponse<OrderVO> createOrder(CreateOrderRequest request) {
        log.info("Creating order for asset: {}, side: {}, size: {}, price: {}",
                request.getAssetName(), request.getSide(), request.getSize(), request.getPrice());

        request.setCustomerId(securityContextManager.getCurrentCustomerId());

        orderValidator.validate(request);

        OrderFactory factory = orderFactoryProvider.getFactory(request);
        Order order = factory.createOrder(request);
        Order createdOrder = orderService.createOrder(order);
        OrderVO orderVO = orderMapper.toVO(createdOrder);

        log.info("Order created successfully with ID: {}", createdOrder.getId());
        return ApiResponse.success(orderVO, "Order created successfully");
    }

    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<OrderVO>> listOrders(ListOrdersRequest request) {
        log.debug("Listing orders with criteria: {}", request);

        if (!securityContextManager.isAdmin()) {
            request.setCustomerId(securityContextManager.getCurrentCustomerId());
        }

        Specification<Order> spec = createOrderSpecification(request);

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                Sort.by(request.getSortDirection(), request.getSortBy())
        );

        Page<Order> orderPage = orderService.findOrders(spec, pageable);
        Page<OrderVO> voPage = orderMapper.toVOPage(orderPage);

        PagedResponse<OrderVO> pagedResponse = PagedResponse.<OrderVO>builder()
                .content(voPage.getContent())
                .pageNumber(voPage.getNumber())
                .pageSize(voPage.getSize())
                .totalElements(voPage.getTotalElements())
                .totalPages(voPage.getTotalPages())
                .first(voPage.isFirst())
                .last(voPage.isLast())
                .empty(voPage.isEmpty())
                .build();

        return ApiResponse.success(pagedResponse);
    }

    @Transactional
    public ApiResponse<OrderVO> cancelOrder(Long orderId) {
        log.info("Canceling order ID: {}", orderId);

        Long customerId = securityContextManager.getCurrentCustomerId();

        Order order = orderService.findById(orderId)
                .orElse(null);

        orderValidator.validateOrderCancellation(order, customerId);

        Order canceledOrder = orderService.cancelOrder(orderId, customerId);
        OrderVO orderVO = orderMapper.toVO(canceledOrder);

        log.info("Order {} canceled successfully", orderId);
        return ApiResponse.success(orderVO, "Order canceled successfully");
    }

    private Specification<Order> createOrderSpecification(ListOrdersRequest request) {
        Specification<Order> spec = OrderSpecifications.hasCustomerId(request.getCustomerId());
        spec = spec.and(OrderSpecifications.hasAssetName(request.getAssetName()));
        spec = spec.and(OrderSpecifications.hasStatus(request.getStatus()));
        spec = spec.and(OrderSpecifications.hasSide(request.getSide()));
        spec = spec.and(OrderSpecifications.betweenDates(request.getStartDate(), request.getEndDate()));
        return spec;
    }
}