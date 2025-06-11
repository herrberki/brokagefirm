package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.repository.OrderRepository;
import com.myproject.brokagefirmchallenge.repo.service.OrderMatchingService;
import com.myproject.brokagefirmchallenge.repo.strategy.PricingStrategy;
import com.myproject.brokagefirmchallenge.repo.strategy.PricingStrategyFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderMatchingServiceImpl implements OrderMatchingService {

    private final OrderRepository orderRepository;
    private final PricingStrategyFactory pricingStrategyFactory;

    @Value("${order.matching.strategy:taker}")
    private String defaultStrategyName;

    private final Map<String, NavigableMap<BigDecimal, Queue<Order>>> buyOrderBooks = new ConcurrentHashMap<>();
    private final Map<String, NavigableMap<BigDecimal, Queue<Order>>> sellOrderBooks = new ConcurrentHashMap<>();

    private record MatchResult(Order buyOrder, Order sellOrder, BigDecimal size, BigDecimal price) {}

    @PostConstruct
    public void initializeOrderBooks() {
        log.info("Initializing order books");
        orderRepository.findAll().stream()
                .filter(order -> order.getStatus().isActive())
                .forEach(this::addOrderToBook);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void matchOrders(String assetName) {
        log.info("Starting order matching for asset: {}", assetName);

        var buyBook = getBuyOrderBookForAsset(assetName);
        var sellBook = getSellOrderBookForAsset(assetName);

        int matchCount = 0;
        MatchResult match;
        while ((match = tryMatchTopOrders(buyBook, sellBook)) != null) {
            processMatch(match);
            matchCount++;
        }

        log.info("Order matching completed for asset: {}. Matches: {}", assetName, matchCount);
    }


    private MatchResult tryMatchTopOrders(NavigableMap<BigDecimal, Queue<Order>> buyBook,
                                          NavigableMap<BigDecimal, Queue<Order>> sellBook) {
        return Optional.ofNullable(buyBook.firstEntry())
                .flatMap(buyEntry -> Optional.ofNullable(sellBook.firstEntry())
                        .flatMap(sellEntry -> createMatchResult(buyEntry, sellEntry)))
                .orElse(null);
    }

    private Optional<MatchResult> createMatchResult(Map.Entry<BigDecimal, Queue<Order>> buyEntry,
                                                    Map.Entry<BigDecimal, Queue<Order>> sellEntry) {
        return Optional.ofNullable(buyEntry.getValue().peek())
                .flatMap(buyOrder -> Optional.ofNullable(sellEntry.getValue().peek())
                        .filter(sellOrder -> canMatch(buyOrder, sellOrder))
                        .map(sellOrder -> new MatchResult(
                                buyOrder,
                                sellOrder,
                                calculateMatchSize(buyOrder, sellOrder),
                                calculateMatchPrice(buyOrder, sellOrder)
                        )));
    }

    private void processMatch(MatchResult match) {
        updateOrderInBook(match.buyOrder, buyOrderBooks);
        updateOrderInBook(match.sellOrder, sellOrderBooks);

    }

    private void updateOrderInBook(Order order, Map<String, NavigableMap<BigDecimal, Queue<Order>>> orderBooks) {
        orderBooks.get(order.getAssetName())
                .computeIfPresent(order.getPrice(), (price, queue) -> {
                    if (order.getRemainingSize().compareTo(BigDecimal.ZERO) == 0) {
                        queue.poll();
                    }
                    return queue.isEmpty() ? null : queue;
                });
    }

    @Override
    public void matchAllPendingOrders() {
        Stream.concat(buyOrderBooks.keySet().stream(), sellOrderBooks.keySet().stream())
                .distinct()
                .forEach(asset -> {
                    try {
                        matchOrders(asset);
                    } catch (Exception e) {
                        log.error("Error matching orders for asset: {}", asset, e);
                    }
                });
    }

    @Override
    public boolean canMatch(Order buyOrder, Order sellOrder) {
        return getStrategy().canMatch(buyOrder, sellOrder);
    }

    @Override
    public BigDecimal calculateMatchSize(Order buyOrder, Order sellOrder) {
        return buyOrder.getRemainingSize().min(sellOrder.getRemainingSize());
    }

    @Override
    public BigDecimal calculateMatchPrice(Order buyOrder, Order sellOrder) {
        return getStrategy().calculateExecutionPrice(buyOrder, sellOrder);
    }

    @Override
    public void addOrderToBook(Order order) {
        log.debug("Adding order {} to order book", order.getId());

        getOrderBook(order).computeIfAbsent(order.getPrice(), k -> new LinkedList<>())
                .offer(order);
    }

    private NavigableMap<BigDecimal, Queue<Order>> getOrderBook(Order order) {
        return order.getOrderSide() == OrderSide.BUY
                ? getBuyOrderBookForAsset(order.getAssetName())
                : getSellOrderBookForAsset(order.getAssetName());
    }

    private NavigableMap<BigDecimal, Queue<Order>> getBuyOrderBookForAsset(String assetName) {
        return buyOrderBooks.computeIfAbsent(assetName,
                k -> new ConcurrentSkipListMap<>(Collections.reverseOrder()));
    }

    private NavigableMap<BigDecimal, Queue<Order>> getSellOrderBookForAsset(String assetName) {
        return sellOrderBooks.computeIfAbsent(assetName,
                k -> new ConcurrentSkipListMap<>());
    }

    private PricingStrategy getStrategy() {
        return pricingStrategyFactory.getStrategy(defaultStrategyName);
    }
}