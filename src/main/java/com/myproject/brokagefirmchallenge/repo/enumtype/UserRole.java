package com.myproject.brokagefirmchallenge.repo.enumtype;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("ADMIN", "Administrator",
            Set.of("ORDER_CREATE", "ORDER_READ", "ORDER_UPDATE", "ORDER_DELETE",
                    "ASSET_READ", "ASSET_UPDATE",
                    "CUSTOMER_CREATE", "CUSTOMER_READ", "CUSTOMER_UPDATE", "CUSTOMER_DELETE",
                    "AUDIT_READ", "ORDER_MATCH")),

    CUSTOMER("CUSTOMER", "Customer",
            Set.of("ORDER_CREATE", "ORDER_READ", "ORDER_DELETE",
                    "ASSET_READ"));

    private final String code;
    private final String description;
    private final Set<String> permissions;

    public Set<GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority("PERMISSION_" + permission))
                .collect(Collectors.toSet());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isCustomer() {
        return this == CUSTOMER;
    }
}