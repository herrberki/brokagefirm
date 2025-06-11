package com.myproject.brokagefirmchallenge.repo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_execution",
        indexes = {
                @Index(name = "idx_execution_order_id", columnList = "order_id"),
                @Index(name = "idx_execution_date", columnList = "execution_date"),
                @Index(name = "idx_execution_matching_order", columnList = "matching_order_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class OrderExecution extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "matching_order_id")
    private Long matchingOrderId;

    @Column(name = "execution_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal executionPrice;

    @Column(name = "execution_size", nullable = false, precision = 19, scale = 4)
    private BigDecimal executionSize;

    @Column(name = "execution_date", nullable = false)
    private LocalDateTime executionDate;

    @Column(name = "buy_order_id", nullable = false)
    private Long buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private Long sellOrderId;

    @Column(name = "buy_customer_id", nullable = false)
    private Long buyCustomerId;

    @Column(name = "sell_customer_id", nullable = false)
    private Long sellCustomerId;

    @Column(name = "commission_amount", precision = 19, scale = 4)
    private BigDecimal commissionAmount = BigDecimal.ZERO;

    @Column(name = "execution_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal executionValue;

    @PrePersist
    public void prePersist() {
        if (this.executionDate == null) {
            this.executionDate = LocalDateTime.now();
        }
        if (this.executionValue == null) {
            this.executionValue = this.executionPrice.multiply(this.executionSize);
        }
    }
}
