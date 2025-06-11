package com.myproject.brokagefirmchallenge.repo.response;

import com.myproject.brokagefirmchallenge.repo.vo.BaseResponse;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class OrderExecutionVO extends BaseResponse {

    private Long executionId;
    private Long buyOrderId;
    private Long sellOrderId;
    private Long buyCustomerId;
    private Long sellCustomerId;
    private BigDecimal executionPrice;
    private BigDecimal executionSize;
    private BigDecimal executionValue;
    private LocalDateTime executionDate;
    private BigDecimal commissionAmount;
}
