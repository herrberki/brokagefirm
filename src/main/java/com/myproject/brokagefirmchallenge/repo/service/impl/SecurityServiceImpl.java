package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.UserPrincipal;
import com.myproject.brokagefirmchallenge.repo.entity.Customer;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.enumtype.CustomerStatus;
import com.myproject.brokagefirmchallenge.repo.exceptions.AuthenticationException;
import com.myproject.brokagefirmchallenge.repo.exceptions.TokenExpiredException;
import com.myproject.brokagefirmchallenge.repo.exceptions.UnauthorizedAccessException;
import com.myproject.brokagefirmchallenge.repo.repository.CustomerRepository;
import com.myproject.brokagefirmchallenge.repo.security.JwtTokenProvider;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import com.myproject.brokagefirmchallenge.repo.service.CustomerService;
import com.myproject.brokagefirmchallenge.repo.service.SecurityService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SecurityServiceImpl implements SecurityService, UserDetailsService {

    private final CustomerRepository customerRepository;
    private final CustomerService customerService;
    private final AuditService auditService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final HttpServletRequest request;

    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    @Override
    public String generateToken(Customer customer) {
        return tokenProvider.generateToken(customer);
    }

    @Override
    public String generateRefreshToken(Customer customer) {
        return tokenProvider.generateRefreshToken(customer);
    }


    @Override
    public Long getCustomerIdFromToken(String token) {
        Claims claims = tokenProvider.getClaimsFromToken(token);
        Object customerIdObj = claims.get("customerId");

        if (customerIdObj instanceof Number) {
            return ((Number) customerIdObj).longValue();
        } else if (customerIdObj instanceof String) {
            return Long.parseLong((String) customerIdObj);
        }

        throw new IllegalArgumentException("Invalid customerId in token");
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        return UserPrincipal.create(customer);
    }

    @Override
    public Map<String, Object> authenticate(String username, String password) {
        log.info("Authentication attempt for username: {}", username);

        try {
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

            if (customer.getIsLocked()) {
                auditService.auditLogin(null, username, getClientIpAddress(), false);
                throw new AuthenticationException("Account is locked");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = generateToken(customer);
            String refreshToken = generateRefreshToken(customer);

            customerService.resetFailedLoginAttempts(username);
            customerService.updateLastLoginDate(customer.getId());

            auditService.auditLogin(customer.getId(), username, getClientIpAddress(), true);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("tokenType", "Bearer");
            response.put("expiresIn", tokenProvider.getExpirationTime());
            response.put("customerId", customer.getId());
            response.put("username", customer.getUsername());
            response.put("role", customer.getRole().name());

            return response;

        } catch (Exception e) {
            log.error("Authentication failed for username: {}", username, e);
            customerService.incrementFailedLoginAttempts(username);
            auditService.auditLogin(null, username, getClientIpAddress(), false);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!tokenProvider.validateRefreshToken(refreshToken)) {
            throw new TokenExpiredException("Refresh token is invalid or expired");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }

        String newAccessToken = generateToken(customer);

        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", tokenProvider.getExpirationTime());

        return response;
    }

    @Override
    public void logout(String token) {
        blacklistToken(token);
        SecurityContextHolder.clearContext();

        try {
            Long customerId = getCustomerIdFromToken(token);
            auditService.auditAction(AuditAction.LOGOUT,
                    "Customer", customerId, null, "User logged out");
        } catch (Exception e) {
            log.error("Failed to audit logout: {}", e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    @Override
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    @Override
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }


    @Override
    public Long getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() &&
                authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getId();
        }

        throw new UnauthorizedAccessException("No authenticated user found");
    }

    @Override
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }

        throw new UnauthorizedAccessException("No authenticated user found");
    }

    private String getClientIpAddress() {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}