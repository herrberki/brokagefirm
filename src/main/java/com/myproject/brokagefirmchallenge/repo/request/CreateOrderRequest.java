package com.myproject.brokagefirmchallenge.repo.request;

import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CreateOrderRequest extends BaseRequest {

    @NotNull(message = "Asset name is required")
    @NotBlank(message = "Asset name cannot be blank")
    private String assetName;

    @NotNull(message = "Order side is required")
    private OrderSide side;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.0001", message = "Size must be at least 0.0001")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
    private BigDecimal price;

    private String orderType;
    private BigDecimal triggerPrice;
    private Map<String, String> metadata;

}
