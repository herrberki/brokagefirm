package com.myproject.brokagefirmchallenge.repo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditAction {
    CREATE("CREATE", "Entity created"),
    UPDATE("UPDATE", "Entity updated"),
    DELETE("DELETE", "Entity deleted"),
    LOGIN("LOGIN", "User logged in"),
    LOGOUT("LOGOUT", "User logged out"),
    LOGIN_FAILED("LOGIN_FAILED", "Login attempt failed"),
    ORDER_PLACED("ORDER_PLACED", "Order placed"),
    ORDER_CANCELED("ORDER_CANCELED", "Order canceled"),
    ORDER_MATCHED("ORDER_MATCHED", "Order matched"),
    ORDER_PARTIALLY_MATCHED("ORDER_PARTIALLY_MATCHED", "Order partially matched"),
    ASSET_BLOCKED("ASSET_BLOCKED", "Asset blocked for order"),
    ASSET_RELEASED("ASSET_RELEASED", "Asset released from order"),
    BALANCE_UPDATED("BALANCE_UPDATED", "Balance updated"),
    PASSWORD_CHANGED("PASSWORD_CHANGED", "Password changed"),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account locked"),
    ACCOUNT_UNLOCKED("ACCOUNT_UNLOCKED", "Account unlocked");

    private final String code;
    private final String description;

    public static AuditAction fromCode(String code) {
        for (AuditAction action : AuditAction.values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("Unknown AuditAction code: " + code);
    }

    public boolean isSecurityRelated() {
        return this == LOGIN || this == LOGOUT || this == LOGIN_FAILED ||
                this == PASSWORD_CHANGED || this == ACCOUNT_LOCKED || this == ACCOUNT_UNLOCKED;
    }

    public boolean isOrderRelated() {
        return this == ORDER_PLACED || this == ORDER_CANCELED ||
                this == ORDER_MATCHED || this == ORDER_PARTIALLY_MATCHED;
    }

    public boolean isAssetRelated() {
        return this == ASSET_BLOCKED || this == ASSET_RELEASED || this == BALANCE_UPDATED;
    }
}
