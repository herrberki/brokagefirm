package com.myproject.brokagefirmchallenge.repo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    PENDING("PENDING", "Order is waiting to be matched"),
    MATCHED("MATCHED", "Order has been fully executed"),
    PARTIALLY_MATCHED("PARTIALLY_MATCHED", "Order has been partially executed"),
    CANCELED("CANCELED", "Order has been canceled");

    private final String code;
    private final String description;


    public boolean isFinal() {
        return this == MATCHED || this == CANCELED;
    }

    public boolean isActive() {
        return this == PENDING || this == PARTIALLY_MATCHED;
    }

    public boolean isCancelable() {
        return this == PENDING || this == PARTIALLY_MATCHED;
    }
}
