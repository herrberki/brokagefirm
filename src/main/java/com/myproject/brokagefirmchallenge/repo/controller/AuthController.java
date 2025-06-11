package com.myproject.brokagefirmchallenge.repo.controller;

import com.myproject.brokagefirmchallenge.repo.manager.AuthManager;
import com.myproject.brokagefirmchallenge.repo.request.LoginRequest;
import com.myproject.brokagefirmchallenge.repo.request.RefreshTokenRequest;
import com.myproject.brokagefirmchallenge.repo.response.ApiResponse;
import com.myproject.brokagefirmchallenge.repo.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthController {

    private final AuthManager authManager;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate and get access token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("REST request to login: {}", request.getUsername());
        ApiResponse<LoginResponse> response = authManager.login(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        log.info("REST request to refresh token");
        ApiResponse<LoginResponse> response = authManager.refreshToken(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout and invalidate token")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader) {

        log.info("REST request to logout");
        ApiResponse<Void> response = authManager.logout(authHeader);

        return ResponseEntity.ok(response);
    }
}
