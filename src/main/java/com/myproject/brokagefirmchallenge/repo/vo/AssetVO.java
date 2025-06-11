package com.myproject.brokagefirmchallenge.repo.vo;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class AssetVO extends BaseResponse {

    private Long assetId;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
    private BigDecimal blockedSize;
    private BigDecimal averageCost;
    private BigDecimal currentMarketValue;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private LocalDateTime lastUpdateDate;
}
