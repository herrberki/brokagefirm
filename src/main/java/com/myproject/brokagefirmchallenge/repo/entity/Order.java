package com.myproject.brokagefirmchallenge.repo.entity;


import com.myproject.brokagefirmchallenge.repo.enumtype.OrderSide;
import com.myproject.brokagefirmchallenge.repo.enumtype.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "orders",
        indexes = {
                @Index(name = "idx_order_customer_id", columnList = "customer_id"),
                @Index(name = "idx_order_asset_name", columnList = "asset_name"),
                @Index(name = "idx_order_status", columnList = "status"),
                @Index(name = "idx_order_create_date", columnList = "created_date"),
                @Index(name = "idx_order_customer_status", columnList = "customer_id, status"),
                @Index(name = "idx_order_side_status", columnList = "order_side, status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "asset_name", nullable = false, length = 10)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_side", nullable = false, length = 10)
    private OrderSide orderSide;

    @Column(name = "size", nullable = false, precision = 19, scale = 4)
    private BigDecimal size;

    @Column(name = "price", nullable = false, precision = 19, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "executed_size", nullable = false, precision = 19, scale = 4)
    private BigDecimal executedSize = BigDecimal.ZERO;

    @Column(name = "remaining_size", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingSize;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Column(name = "average_execution_price", precision = 19, scale = 4)
    private BigDecimal averageExecutionPrice;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @ElementCollection
    @CollectionTable(name = "order_metadata",
            joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();


    @PrePersist
    public void prePersist() {
        if (this.remainingSize == null) {
            this.remainingSize = this.size;
        }
        if (this.totalAmount == null) {
            this.totalAmount = this.size.multiply(this.price);
        }
    }
}
