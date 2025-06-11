package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import com.myproject.brokagefirmchallenge.repo.repository.OrderRepository;
import com.myproject.brokagefirmchallenge.repo.strategy.PricingStrategy;
import com.myproject.brokagefirmchallenge.repo.strategy.PricingStrategyFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class OrderMatchingServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PricingStrategyFactory pricingStrategyFactory;

    @Mock
    private PricingStrategy pricingStrategy;

    @InjectMocks
    private OrderMatchingServiceImpl orderMatchingService;

    private Order buyOrder;
    private Order sellOrder;
    private static final String ASSET_NAME = "BTC";
    private static final BigDecimal BUY_PRICE = new BigDecimal("50000");
    private static final BigDecimal SELL_PRICE = new BigDecimal("49000");
    private static final BigDecimal ORDER_SIZE = new BigDecimal("1.0");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderMatchingService, "defaultStrategyName", "taker");

        when(pricingStrategyFactory.getStrategy(anyString())).thenReturn(pricingStrategy);

        buyOrder = createOrder(1L, ASSET_NAME, BUY_PRICE, ORDER_SIZE, OrderSide.BUY);
        sellOrder = createOrder(2L, ASSET_NAME, SELL_PRICE, ORDER_SIZE, OrderSide.SELL);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("Should initialize order books with active orders on post construct")
    void should_initialize_order_books_with_active_orders() {
        // given
        List<Order> activeOrders = Arrays.asList(buyOrder, sellOrder);
        when(orderRepository.findAll()).thenReturn(activeOrders);

        // when
        orderMatchingService.initializeOrderBooks();

        // then
        verify(orderRepository).findAll();
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("Should add buy order to buy order book")
    void should_add_buy_order_to_buy_order_book() {
        // given

        // when
        orderMatchingService.addOrderToBook(buyOrder);

        // then
        Map<String, NavigableMap<BigDecimal, Queue<Order>>> buyOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "buyOrderBooks");

        assertNotNull(buyOrderBooks);
        assertTrue(buyOrderBooks.containsKey(ASSET_NAME));
        assertTrue(buyOrderBooks.get(ASSET_NAME).containsKey(BUY_PRICE));
        assertEquals(buyOrder, buyOrderBooks.get(ASSET_NAME).get(BUY_PRICE).peek());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("Should add sell order to sell order book")
    void should_add_sell_order_to_sell_order_book() {
        // given

        // when
        orderMatchingService.addOrderToBook(sellOrder);

        // then
        Map<String, NavigableMap<BigDecimal, Queue<Order>>> sellOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "sellOrderBooks");

        assertNotNull(sellOrderBooks);
        assertTrue(sellOrderBooks.containsKey(ASSET_NAME));
        assertTrue(sellOrderBooks.get(ASSET_NAME).containsKey(SELL_PRICE));
        assertEquals(sellOrder, sellOrderBooks.get(ASSET_NAME).get(SELL_PRICE).peek());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("Should check if orders can match using pricing strategy")
    void should_check_if_orders_can_match_using_pricing_strategy() {
        // given
        when(pricingStrategy.canMatch(buyOrder, sellOrder)).thenReturn(true);

        // when
        boolean result = orderMatchingService.canMatch(buyOrder, sellOrder);

        // then
        assertTrue(result);
        verify(pricingStrategy).canMatch(buyOrder, sellOrder);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("Should calculate match size as minimum of remaining sizes")
    void should_calculate_match_size_as_minimum_of_remaining_sizes() {
        // given
        buyOrder.setRemainingSize(new BigDecimal("1.5"));
        sellOrder.setRemainingSize(new BigDecimal("0.8"));

        // when
        BigDecimal matchSize = orderMatchingService.calculateMatchSize(buyOrder, sellOrder);

        // then
        assertEquals(new BigDecimal("0.8"), matchSize);
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("Should calculate match price using pricing strategy")
    void should_calculate_match_price_using_pricing_strategy() {
        // given
        BigDecimal expectedPrice = new BigDecimal("49500");
        when(pricingStrategy.calculateExecutionPrice(buyOrder, sellOrder)).thenReturn(expectedPrice);

        // when
        BigDecimal matchPrice = orderMatchingService.calculateMatchPrice(buyOrder, sellOrder);

        // then
        assertEquals(expectedPrice, matchPrice);
        verify(pricingStrategy).calculateExecutionPrice(buyOrder, sellOrder);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("Should match orders when buy price >= sell price")
    void should_match_orders_when_prices_cross() {
        // given
        buyOrder.setRemainingSize(ORDER_SIZE);
        sellOrder.setRemainingSize(ORDER_SIZE);

        when(pricingStrategy.canMatch(any(), any())).thenReturn(true);
        when(pricingStrategy.calculateExecutionPrice(any(), any())).thenReturn(BUY_PRICE);

        orderMatchingService.addOrderToBook(buyOrder);
        orderMatchingService.addOrderToBook(sellOrder);

        // when
        orderMatchingService.matchOrders(ASSET_NAME);

        // then
        verify(pricingStrategy, atLeastOnce()).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(8)
    @DisplayName("Should not match orders when buy price < sell price")
    void should_not_match_orders_when_prices_do_not_cross() {
        // given
        buyOrder = createOrder(1L, ASSET_NAME, new BigDecimal("48000"), ORDER_SIZE, OrderSide.BUY);
        sellOrder = createOrder(2L, ASSET_NAME, new BigDecimal("50000"), ORDER_SIZE, OrderSide.SELL);

        when(pricingStrategy.canMatch(any(), any())).thenReturn(false);

        orderMatchingService.addOrderToBook(buyOrder);
        orderMatchingService.addOrderToBook(sellOrder);

        // when
        orderMatchingService.matchOrders(ASSET_NAME);

        // then
        verify(pricingStrategy, atMost(1)).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(9)
    @DisplayName("Should match multiple orders in price-time priority")
    void should_match_multiple_orders_in_price_time_priority() {
        // given
        Order buyOrder1 = createOrder(1L, ASSET_NAME, new BigDecimal("51000"), ORDER_SIZE, OrderSide.BUY);
        Order buyOrder2 = createOrder(2L, ASSET_NAME, new BigDecimal("50000"), ORDER_SIZE, OrderSide.BUY);
        Order sellOrder1 = createOrder(3L, ASSET_NAME, new BigDecimal("49000"), ORDER_SIZE, OrderSide.SELL);
        Order sellOrder2 = createOrder(4L, ASSET_NAME, new BigDecimal("50000"), ORDER_SIZE, OrderSide.SELL);

        when(pricingStrategy.canMatch(any(), any())).thenReturn(true);
        when(pricingStrategy.calculateExecutionPrice(any(), any())).thenReturn(new BigDecimal("50000"));

        orderMatchingService.addOrderToBook(buyOrder1);
        orderMatchingService.addOrderToBook(buyOrder2);
        orderMatchingService.addOrderToBook(sellOrder1);
        orderMatchingService.addOrderToBook(sellOrder2);

        // when
        orderMatchingService.matchOrders(ASSET_NAME);

        // then
        verify(pricingStrategy, atLeastOnce()).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(10)
    @DisplayName("Should match all pending orders for all assets")
    void should_match_all_pending_orders_for_all_assets() {
        // given
        Order btcBuyOrder = createOrder(1L, "BTC", BUY_PRICE, ORDER_SIZE, OrderSide.BUY);
        Order btcSellOrder = createOrder(2L, "BTC", SELL_PRICE, ORDER_SIZE, OrderSide.SELL);
        Order ethBuyOrder = createOrder(3L, "ETH", new BigDecimal("3000"), ORDER_SIZE, OrderSide.BUY);
        Order ethSellOrder = createOrder(4L, "ETH", new BigDecimal("2900"), ORDER_SIZE, OrderSide.SELL);

        when(pricingStrategy.canMatch(any(), any())).thenReturn(true);
        when(pricingStrategy.calculateExecutionPrice(any(), any())).thenReturn(BUY_PRICE);

        orderMatchingService.addOrderToBook(btcBuyOrder);
        orderMatchingService.addOrderToBook(btcSellOrder);
        orderMatchingService.addOrderToBook(ethBuyOrder);
        orderMatchingService.addOrderToBook(ethSellOrder);

        // when
        orderMatchingService.matchAllPendingOrders();

        // then
        verify(pricingStrategy, atLeastOnce()).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(11)
    @DisplayName("Should handle partial fills correctly")
    void should_handle_partial_fills_correctly() {
        // given
        buyOrder.setRemainingSize(new BigDecimal("2.0"));
        sellOrder.setRemainingSize(new BigDecimal("1.0"));

        when(pricingStrategy.canMatch(any(), any())).thenReturn(true);
        when(pricingStrategy.calculateExecutionPrice(any(), any())).thenReturn(BUY_PRICE);

        orderMatchingService.addOrderToBook(buyOrder);
        orderMatchingService.addOrderToBook(sellOrder);

        // when
        orderMatchingService.matchOrders(ASSET_NAME);

        // then
        Map<String, NavigableMap<BigDecimal, Queue<Order>>> buyOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "buyOrderBooks");

        assertTrue(buyOrderBooks.get(ASSET_NAME).containsKey(BUY_PRICE));
    }

    @Test
    @org.junit.jupiter.api.Order(12)
    @DisplayName("Should handle empty order books gracefully")
    void should_handle_empty_order_books_gracefully() {
        // given

        // when & then
        assertDoesNotThrow(() -> orderMatchingService.matchOrders(ASSET_NAME));

        // then
        verify(pricingStrategy, never()).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(13)
    @DisplayName("Should handle exception during matching for specific asset")
    void should_handle_exception_during_matching_for_specific_asset() {
        // given
        orderMatchingService.addOrderToBook(buyOrder);
        orderMatchingService.addOrderToBook(sellOrder);

        when(pricingStrategy.canMatch(any(), any())).thenThrow(new RuntimeException("Test exception"));

        // when
        assertDoesNotThrow(() -> orderMatchingService.matchAllPendingOrders());

        // then
        verify(pricingStrategy, atLeastOnce()).canMatch(any(), any());
    }

    @Test
    @org.junit.jupiter.api.Order(14)
    @DisplayName("Should maintain order book integrity with concurrent operations")
    void should_maintain_order_book_integrity_with_concurrent_operations() {
        // given
        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            orders.add(createOrder((long) i, ASSET_NAME, BUY_PRICE.add(new BigDecimal(i)), ORDER_SIZE, OrderSide.BUY));
        }

        // when
        orders.parallelStream().forEach(order -> orderMatchingService.addOrderToBook(order));

        // then
        Map<String, NavigableMap<BigDecimal, Queue<Order>>> buyOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "buyOrderBooks");

        assert buyOrderBooks != null;
        assertNotNull(buyOrderBooks.get(ASSET_NAME));
        assertEquals(10, buyOrderBooks.get(ASSET_NAME).size());
    }

    @Test
    @org.junit.jupiter.api.Order(15)
    @DisplayName("Should remove fully filled orders from order book")
    void should_remove_fully_filled_orders_from_order_book() {
        // given
        buyOrder.setRemainingSize(BigDecimal.ZERO);
        sellOrder.setRemainingSize(BigDecimal.ZERO);

        orderMatchingService.addOrderToBook(buyOrder);
        orderMatchingService.addOrderToBook(sellOrder);

        Map<String, NavigableMap<BigDecimal, Queue<Order>>> buyOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "buyOrderBooks");
        Map<String, NavigableMap<BigDecimal, Queue<Order>>> sellOrderBooks =
                (Map<String, NavigableMap<BigDecimal, Queue<Order>>>) ReflectionTestUtils.getField(orderMatchingService, "sellOrderBooks");

        // when
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateOrderInBook", buyOrder, buyOrderBooks);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateOrderInBook", sellOrder, sellOrderBooks);

        // then
        assertFalse(buyOrderBooks.get(ASSET_NAME).containsKey(BUY_PRICE));
        assertFalse(sellOrderBooks.get(ASSET_NAME).containsKey(SELL_PRICE));
    }

    @Test
    @org.junit.jupiter.api.Order(16)
    @DisplayName("Should return correct pricing strategy")
    void should_return_correct_pricing_strategy() {
        // given
        String strategyName = "maker";
        ReflectionTestUtils.setField(orderMatchingService, "defaultStrategyName", strategyName);
        PricingStrategy expectedStrategy = mock(PricingStrategy.class);
        when(pricingStrategyFactory.getStrategy(strategyName)).thenReturn(expectedStrategy);

        // when
        PricingStrategy actualStrategy = (PricingStrategy) ReflectionTestUtils.invokeMethod(orderMatchingService, "getStrategy");

        // then
        assertEquals(expectedStrategy, actualStrategy);
        verify(pricingStrategyFactory).getStrategy(strategyName);
    }

    private Order createOrder(Long id, String assetName, BigDecimal price, BigDecimal size, OrderSide side) {
        Order order = new Order();
        order.setId(id);
        order.setAssetName(assetName);
        order.setPrice(price);
        order.setSize(size);
        order.setRemainingSize(size);
        order.setOrderSide(side);
        order.setStatus(OrderStatus.MATCHED);
        return order;
    }
}