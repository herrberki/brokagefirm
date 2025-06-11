package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.brokagefirmchallenge.repo.entity.AuditLog;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.repository.AuditLogRepository;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import com.myproject.brokagefirmchallenge.repo.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityService securityService;
    private final ObjectMapper objectMapper;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditCreate(String entityName, Long entityId, Object newValue) {
        try {
            AuditLog auditLog = createBaseAuditLog(entityName, entityId, AuditAction.CREATE);
            auditLog.setNewValue(objectToJson(newValue));
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit create action for {}: {}", entityName, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditUpdate(String entityName, Long entityId, Object oldValue, Object newValue) {
        try {
            AuditLog auditLog = createBaseAuditLog(entityName, entityId, AuditAction.UPDATE);
            auditLog.setOldValue(objectToJson(oldValue));
            auditLog.setNewValue(objectToJson(newValue));
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit update action for {}: {}", entityName, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditAction(AuditAction action, String entityName, Long entityId,
                            String oldValue, String newValue) {
        try {
            AuditLog auditLog = createBaseAuditLog(entityName, entityId, action);
            auditLog.setOldValue(oldValue);
            auditLog.setNewValue(newValue);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit action {} for {}: {}", action, entityName, e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditLogin(Long userId, String username, String ipAddress, boolean success) {
        try {
            AuditAction action = success ? AuditAction.LOGIN : AuditAction.LOGIN_FAILED;
            AuditLog auditLog = AuditLog.builder()
                    .entityName("Customer")
                    .entityId(userId)
                    .action(action)
                    .userId(userId != null ? userId : 0L)
                    .username(username)
                    .actionDate(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(getUserAgent())
                    .requestId(getRequestId())
                    .newValue(success ? "Login successful" : "Login failed")
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit login for user {}: {}", username, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditOrderAction(Long orderId, AuditAction action, String details) {
        try {
            AuditLog auditLog = createBaseAuditLog("Order", orderId, action);
            auditLog.setNewValue(details);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit order action for order {}: {}", orderId, e.getMessage());
        }
    }

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditBalanceChange(Long customerId, String assetName,
                                   BigDecimal oldBalance, BigDecimal newBalance, String reason) {
        try {

            AuditLog auditLog = createBaseAuditLog("Asset", customerId, AuditAction.BALANCE_UPDATED);
            auditLog.setOldValue(oldBalance.toString());
            auditLog.setNewValue(newBalance.toString());
            auditLog.setEntityName("Asset-" + assetName);
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to audit balance change for customer {}: {}", customerId, e.getMessage());
        }
    }

    private AuditLog createBaseAuditLog(String entityName, Long entityId, AuditAction action) {
        Long currentUserId = getCurrentUserId();
        String currentUsername = getCurrentUsername();

        return AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .userId(currentUserId != null ? currentUserId : 0L)
                .username(currentUsername != null ? currentUsername : "system")
                .actionDate(LocalDateTime.now())
                .ipAddress(getClientIpAddress())
                .userAgent(getUserAgent())
                .requestId(getRequestId())
                .build();
    }

    private String objectToJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to convert object to JSON: {}", e.getMessage());
            return obj.toString();
        }
    }

    private Long getCurrentUserId() {
        try {
            return securityService.getCurrentCustomerId();
        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentUsername() {
        try {
            return securityService.getCurrentUsername();
        } catch (Exception e) {
            return "system";
        }
    }

    private String getClientIpAddress() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            if (request != null) {
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
        } catch (Exception e) {
            log.debug("Failed to get client IP: {}", e.getMessage());
        }
        return "unknown";
    }

    private String getUserAgent() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            if (request != null) {
                return request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Failed to get user agent: {}", e.getMessage());
        }
        return "unknown";
    }

    private String getRequestId() {
        try {
            HttpServletRequest request = getHttpServletRequest();
            if (request != null) {
                String requestId = request.getHeader("X-Request-ID");
                return requestId != null ? requestId : generateRequestId();
            }
        } catch (Exception e) {
            log.debug("Failed to get request ID: {}", e.getMessage());
        }
        return generateRequestId();
    }

    private String generateRequestId() {
        return java.util.UUID.randomUUID().toString();
    }

    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}