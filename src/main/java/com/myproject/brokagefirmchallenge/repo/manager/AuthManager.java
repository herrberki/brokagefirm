package com.myproject.brokagefirmchallenge.repo.manager;

import com.myproject.brokagefirmchallenge.repo.request.LoginRequest;
import com.myproject.brokagefirmchallenge.repo.request.RefreshTokenRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.response.LoginResponse;
import com.myproject.brokagefirmchallenge.repo.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthManager {

    private final SecurityService securityService;

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        Map<String, Object> authResult = securityService.authenticate(
                request.getUsername(), request.getPassword());

        LoginResponse response = LoginResponse.builder()
                .accessToken((String) authResult.get("accessToken"))
                .refreshToken((String) authResult.get("refreshToken"))
                .tokenType((String) authResult.get("tokenType"))
                .expiresIn((Long) authResult.get("expiresIn"))
                .customerId((Long) authResult.get("customerId"))
                .username((String) authResult.get("username"))
                .role((String) authResult.get("role"))
                .build();

        log.info("Login successful for username: {}", request.getUsername());
        return ApiResponse.success(response, "Login successful");
    }

    public ApiResponse<LoginResponse> refreshToken(RefreshTokenRequest request) {
        log.debug("Refreshing token");

        Map<String, Object> refreshResult = securityService.refreshToken(request.getRefreshToken());

        LoginResponse response = LoginResponse.builder()
                .accessToken((String) refreshResult.get("accessToken"))
                .tokenType((String) refreshResult.get("tokenType"))
                .expiresIn((Long) refreshResult.get("expiresIn"))
                .build();

        return ApiResponse.success(response, "Token refreshed successfully");
    }

    public ApiResponse<Void> logout(String authHeader) {
        log.info("Logout request");

        String token = securityService.extractTokenFromHeader(authHeader);
        if (token != null) {
            securityService.logout(token);
        }

        return ApiResponse.success(null, "Logged out successfully");
    }
}
