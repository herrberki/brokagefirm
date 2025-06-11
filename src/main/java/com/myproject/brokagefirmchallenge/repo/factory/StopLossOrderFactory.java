package com.myproject.brokagefirmchallenge.repo.factory;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import com.myproject.brokagefirmchallenge.repo.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class StopLossOrderFactory extends AbstractOrderFactory {

    private final AssetService assetService;
    private final MarketDataService marketDataService;

    @Override
    public boolean supports(CreateOrderRequest request) {
        return request.getOrderType() != null &&
                request.getOrderType().equals("STOP_LOSS");
    }

    @Override
    protected void validateRequest(CreateOrderRequest request) {
        validateCommonFields(request);

        if (request.getSide() != OrderSide.SELL) {
            throw new InvalidOrderException("Stop loss orders can only be SELL orders");
        }

        BigDecimal currentBalance = assetService.getTotalBalance(
                request.getCustomerId(),
                request.getAssetName()
        );

        if (currentBalance.compareTo(request.getSize()) < 0) {
            throw new InvalidOrderException("Insufficient asset balance for stop loss order");
        }

        BigDecimal currentPrice = marketDataService.getCurrentPrice(request.getAssetName());
        if (request.getPrice().compareTo(currentPrice) >= 0) {
            throw new InvalidOrderException("Stop loss price must be below current market price");
        }
    }
    @Override
    protected void enrichOrder(Order order, CreateOrderRequest request) {
        order.getMetadata().put("orderType", "STOP_LOSS");
        order.getMetadata().put("stopPrice", request.getPrice().toPlainString());

        if (request.getTriggerPrice() != null) {
            order.getMetadata().put("triggerPrice", request.getTriggerPrice().toPlainString());
        }
    }
}