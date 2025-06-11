package com.myproject.brokagefirmchallenge.repo.converter;

import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import com.myproject.brokagefirmchallenge.repo.service.MarketDataService;
import com.myproject.brokagefirmchallenge.repo.vo.AssetVO;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper(config = CentralMapperConfig.class)
public abstract class AssetMapper implements BaseMapper<Asset, Void, AssetVO> {

    @Autowired
    protected MarketDataService marketDataService;

    @Override
    public Asset toEntity(Void dto) {
        throw new UnsupportedOperationException("Asset creation not supported through mapper");
    }

    @Mapping(target = "assetId", source = "id")
    @Mapping(target = "blockedSize", expression = "java(entity.getSize().subtract(entity.getUsableSize()))")
    @Mapping(target = "lastUpdateDate", source = "updatedDate")
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "traceId", ignore = true)
    public abstract AssetVO toVO(Asset entity);

    @AfterMapping
    protected void enrichWithMarketData(@MappingTarget AssetVO vo, Asset entity) {
        try {
            BigDecimal currentPrice = marketDataService.getCurrentPrice(entity.getAssetName());

            if (currentPrice != null && entity.getAverageCost() != null) {
                vo.setCurrentMarketValue(entity.getSize().multiply(currentPrice));

                BigDecimal totalCost = entity.getSize().multiply(entity.getAverageCost());
                BigDecimal profitLoss = vo.getCurrentMarketValue().subtract(totalCost);
                vo.setProfitLoss(profitLoss);

                if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal plPercentage = profitLoss.divide(totalCost, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    vo.setProfitLossPercentage(plPercentage);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
