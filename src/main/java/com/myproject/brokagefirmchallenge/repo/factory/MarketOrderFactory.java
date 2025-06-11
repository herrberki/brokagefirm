package com.myproject.brokagefirmchallenge.repo.factory;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class MarketOrderFactory extends AbstractOrderFactory {

    private final MarketDataService marketDataService;

    @Override
    public boolean supports(CreateOrderRequest request) {
        return request.getPrice() == null ||
                request.getPrice().compareTo(BigDecimal.ZERO) == 0;
    }

    @Override
    protected void validateRequest(CreateOrderRequest request) {
        validateCommonFields(request);

        BigDecimal maxMarketOrderSize = new BigDecimal("10000");
        if (request.getSize().compareTo(maxMarketOrderSize) > 0) {
            throw new InvalidOrderException("Market order size cannot exceed " + maxMarketOrderSize);
        }
    }

    @Override
    protected void enrichOrder(Order order, CreateOrderRequest request) {
        BigDecimal currentPrice = marketDataService.getCurrentPrice(request.getAssetName());

        BigDecimal slippage = request.getSide() == OrderSide.BUY
                ? BigDecimal.valueOf(1.01)
                : BigDecimal.valueOf(0.99);

        BigDecimal marketPrice = currentPrice.multiply(slippage);

        order.setPrice(marketPrice);
        order.setTotalAmount(order.getSize().multiply(marketPrice));
    }
}
