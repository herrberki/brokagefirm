package com.myproject.brokagefirmchallenge.repo.converter;

import com.myproject.brokagefirmchallenge.repo.entity.Order;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import com.myproject.brokagefirmchallenge.repo.request.CreateOrderRequest;
import com.myproject.brokagefirmchallenge.repo.vo.OrderVO;
import org.mapstruct.*;

@Mapper(config = CentralMapperConfig.class)
public interface OrderMapper extends BaseMapper<Order, CreateOrderRequest, OrderVO> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "executedSize", expression = "java(BigDecimal.ZERO)")
    @Mapping(target = "remainingSize", source = "size")
    @Mapping(target = "totalAmount", expression = "java(dto.getSize().multiply(dto.getPrice()))")
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "orderSide", source = "side")
    Order toEntity(CreateOrderRequest dto);

    @Mapping(target = "orderId", source = "id")
    @Mapping(target = "createDate", source = "createdDate")
    @Mapping(target = "updateDate", source = "updatedDate")
    @Mapping(target = "side", source = "orderSide")
    @Mapping(target = "statusDescription", expression = "java(getStatusDescription(entity.getStatus()))")
    @Mapping(target = "timestamp", ignore = true)
    @Mapping(target = "traceId", ignore = true)
    OrderVO toVO(Order entity);

    default String getStatusDescription(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Order is waiting to be matched";
            case MATCHED -> "Order has been fully executed";
            case PARTIALLY_MATCHED -> "Order has been partially executed";
            case CANCELED -> "Order has been canceled";
        };
    }
}
