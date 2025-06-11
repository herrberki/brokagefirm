package com.myproject.brokagefirmchallenge.repo.observer;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class BalanceUpdateEvent extends ApplicationEvent {

    private final Long customerId;
    private final String assetName;
    private final BigDecimal oldBalance;
    private final BigDecimal newBalance;
    private final String updateReason;
    private final LocalDateTime occurredAt;

    public BalanceUpdateEvent(Object source, Long customerId, String assetName,
                              BigDecimal oldBalance, BigDecimal newBalance, String updateReason) {
        super(source);
        this.customerId = customerId;
        this.assetName = assetName;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
        this.updateReason = updateReason;
        this.occurredAt = LocalDateTime.now();
    }
}
