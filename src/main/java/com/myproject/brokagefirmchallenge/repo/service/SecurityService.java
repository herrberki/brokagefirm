package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.entity.Customer;

import java.util.Map;

public interface SecurityService {

    String generateToken(Customer customer);

    String generateRefreshToken(Customer customer);

    Long getCustomerIdFromToken(String token);

    Map<String, Object> authenticate(String username, String password);

    Map<String, Object> refreshToken(String refreshToken);

    void logout(String token);

    boolean isTokenBlacklisted(String token);

    void blacklistToken(String token);

    String extractTokenFromHeader(String authHeader);

    Long getCurrentCustomerId();

    String getCurrentUsername();
}