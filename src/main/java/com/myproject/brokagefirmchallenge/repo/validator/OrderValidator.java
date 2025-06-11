package com.myproject.brokagefirmchallenge.repo.validator;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.exceptions.InsufficientBalanceException;
import com.myproject.brokagefirmchallenge.repo.exceptions.InvalidOrderException;
import com.myproject.brokagefirmchallenge.repo.exceptions.ValidationException;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class OrderValidator extends BaseValidator<CreateOrderRequest> {

    private final AssetService assetService;

    private static final String TRY_ASSET = "TRY";
    private static final BigDecimal MIN_ORDER_SIZE = new BigDecimal("0.0001");
    private static final BigDecimal MIN_ORDER_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_ORDER_SIZE = new BigDecimal("1000000");
    private static final BigDecimal MAX_ORDER_PRICE = new BigDecimal("999999");

    @Override
    public void validate(CreateOrderRequest request) {
        validateBasicFields(request);
        validateOrderLimits(request);
        validateAssetName(request);
        validateBalance(request);
    }

    private void validateBasicFields(CreateOrderRequest request) {
        validateNotNull(request.getAssetName(), "Asset name");
        validateNotNull(request.getSide(), "Order side");
        validateNotNull(request.getSize(), "Size");
        validateNotNull(request.getPrice(), "Price");
        validateNotNull(request.getCustomerId(), "Customer ID");
    }

    private void validateOrderLimits(CreateOrderRequest request) {
        if (request.getSize().compareTo(MIN_ORDER_SIZE) < 0) {
            throw new InvalidOrderException(
                    String.format("Order size must be at least %s", MIN_ORDER_SIZE)
            );
        }

        if (request.getSize().compareTo(MAX_ORDER_SIZE) > 0) {
            throw new InvalidOrderException(
                    String.format("Order size cannot exceed %s", MAX_ORDER_SIZE)
            );
        }

        if (request.getPrice().compareTo(MIN_ORDER_PRICE) < 0) {
            throw new InvalidOrderException(
                    String.format("Order price must be at least %s", MIN_ORDER_PRICE)
            );
        }

        if (request.getPrice().compareTo(MAX_ORDER_PRICE) > 0) {
            throw new InvalidOrderException(
                    String.format("Order price cannot exceed %s", MAX_ORDER_PRICE)
            );
        }
    }

    private void validateAssetName(CreateOrderRequest request) {
        if (TRY_ASSET.equals(request.getAssetName())) {
            throw new InvalidOrderException("Cannot trade TRY against TRY");
        }

        if (!request.getAssetName().matches("^[A-Z]{2,10}$")) {
            throw new InvalidOrderException("Invalid asset name format");
        }
    }

    private void validateBalance(CreateOrderRequest request) {
        if (request.getSide() == OrderSide.BUY) {
            BigDecimal requiredTry = request.getSize().multiply(request.getPrice());

            if (!assetService.isEnoughBalance(request.getCustomerId(), TRY_ASSET, requiredTry)) {
                BigDecimal availableBalance = assetService.getUsableBalance(request.getCustomerId(), TRY_ASSET);
                throw new InsufficientBalanceException(
                        String.format("Insufficient TRY balance. Required: %s, Available: %s",
                                requiredTry, availableBalance)
                );
            }
        } else {
            if (!assetService.isEnoughBalance(request.getCustomerId(), request.getAssetName(), request.getSize())) {
                BigDecimal availableBalance = assetService.getUsableBalance(request.getCustomerId(), request.getAssetName());
                throw new InsufficientBalanceException(
                        String.format("Insufficient %s balance. Required: %s, Available: %s",
                                request.getAssetName(), request.getSize(), availableBalance)
                );
            }
        }
    }

    public void validateOrderCancellation(Order order, Long customerId) {
        if (order == null) {
            throw new ValidationException("Order not found");
        }

        if (!order.getCustomerId().equals(customerId)) {
            throw new ValidationException("You are not authorized to cancel this order");
        }

        if (!order.getStatus().isCancelable()) {
            throw new InvalidOrderException(
                    String.format("Order cannot be canceled in status: %s", order.getStatus())
            );
        }
    }
}
