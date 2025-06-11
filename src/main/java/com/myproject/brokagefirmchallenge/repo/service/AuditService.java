package com.myproject.brokagefirmchallenge.repo.service;

import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;

import java.math.BigDecimal;

public interface AuditService {

    void auditCreate(String entityName, Long entityId, Object newValue);

    void auditUpdate(String entityName, Long entityId, Object oldValue, Object newValue);

    void auditAction(AuditAction action, String entityName, Long entityId,
                     String oldValue, String newValue);

    void auditLogin(Long userId, String username, String ipAddress, boolean success);

    void auditOrderAction(Long orderId, AuditAction action, String details);

    void auditBalanceChange(Long customerId, String assetName,
                            BigDecimal oldBalance, BigDecimal newBalance, String reason);
}
