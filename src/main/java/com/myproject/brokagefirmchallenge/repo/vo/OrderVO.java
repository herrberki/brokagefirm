package com.myproject.brokagefirmchallenge.repo.vo;

import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class OrderVO extends BaseResponse {

    private Long orderId;
    private Long customerId;
    private String assetName;
    private OrderSide side;
    private BigDecimal size;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createDate;
    private LocalDateTime updateDate;
    private BigDecimal executedSize;
    private BigDecimal remainingSize;
    private BigDecimal averageExecutionPrice;
    private String statusDescription;
}
