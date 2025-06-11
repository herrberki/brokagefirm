package com.myproject.brokagefirmchallenge.repo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerStatus {
    ACTIVE("ACTIVE", "Customer is active"),
    INACTIVE("INACTIVE", "Customer is inactive"),
    SUSPENDED("SUSPENDED", "Customer is temporarily suspended"),
    BLOCKED("BLOCKED", "Customer is blocked"),
    PENDING_VERIFICATION("PENDING_VERIFICATION", "Customer is pending verification"),
    DELETED("DELETED", "Customer is soft deleted");

    private final String code;
    private final String description;

    public static CustomerStatus fromCode(String code) {
        for (CustomerStatus status : CustomerStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerStatus code: " + code);
    }

    public boolean canLogin() {
        return this == ACTIVE;
    }

    public boolean canTrade() {
        return this == ACTIVE;
    }

    public boolean isRestricted() {
        return this == SUSPENDED || this == BLOCKED || this == INACTIVE;
    }

    public boolean isPending() {
        return this == PENDING_VERIFICATION;
    }

    public boolean isDeleted() {
        return this == DELETED;
    }
}
