package com.myproject.brokagefirmchallenge.repo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "asset",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"customer_id", "asset_name"})
        },
        indexes = {
                @Index(name = "idx_asset_customer_id", columnList = "customer_id"),
                @Index(name = "idx_asset_name", columnList = "asset_name"),
                @Index(name = "idx_asset_customer_asset", columnList = "customer_id, asset_name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class Asset extends BaseEntity {

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "asset_name", nullable = false, length = 10)
    private String assetName;

    @Column(name = "size", nullable = false, precision = 19, scale = 4)
    private BigDecimal size = BigDecimal.ZERO;

    @Column(name = "usable_size", nullable = false, precision = 19, scale = 4)
    private BigDecimal usableSize = BigDecimal.ZERO;

    @Column(name = "average_cost", precision = 19, scale = 4)
    private BigDecimal averageCost = BigDecimal.ZERO;

    @Column(name = "blocked_size", precision = 19, scale = 4)
    private BigDecimal blockedSize = BigDecimal.ZERO;

}
