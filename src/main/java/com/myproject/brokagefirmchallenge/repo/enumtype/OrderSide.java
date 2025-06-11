package com.myproject.brokagefirmchallenge.repo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderSide {
    BUY("BUY", "Buy Order"),
    SELL("SELL", "Sell Order");

    private final String code;
    private final String description;
}
