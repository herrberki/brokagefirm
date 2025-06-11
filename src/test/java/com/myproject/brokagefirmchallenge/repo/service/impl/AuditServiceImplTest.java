package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.brokagefirmchallenge.repo.entity.AuditLog;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.repository.AuditLogRepository;
import com.myproject.brokagefirmchallenge.repo.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private SecurityService securityService;
    @Mock private ObjectMapper objectMapper;
    @Spy @InjectMocks private AuditServiceImpl auditService;
    @Mock private HttpServletRequest request;

    private final Long USER_ID = 42L;
    private final String USERNAME = "alice";
    private final String IP = "1.2.3.4";
    private final String USER_AGENT = "TestAgent";
    private final String REQUEST_ID = UUID.randomUUID().toString();

    @BeforeEach
    void setupRequestContext() {
        when(securityService.getCurrentCustomerId()).thenReturn(USER_ID);
        when(securityService.getCurrentUsername()).thenReturn(USERNAME);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(IP);
        when(request.getHeader("User-Agent")).thenReturn(USER_AGENT);
        when(request.getHeader("X-Request-ID")).thenReturn(REQUEST_ID);
        ServletRequestAttributes attrs = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attrs);
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{\"foo\":\"bar\"}");
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("auditCreate should build CREATE log with newValue JSON")
    void should_auditCreate_saveCorrectLog() {
        // when
        auditService.auditCreate("Ent", 99L, new Object());
        // then
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLog log = cap.getValue();
        assertThat(log.getEntityName()).isEqualTo("Ent");
        assertThat(log.getEntityId()).isEqualTo(99L);
        assertThat(log.getAction()).isEqualTo(AuditAction.CREATE);
        assertThat(log.getOldValue()).isNull();
        assertThat(log.getNewValue()).isEqualTo("{\"foo\":\"bar\"}");
        assertCommon(log);
    }

    @Test
    @DisplayName("auditUpdate should build UPDATE log with both JSON values")
    void should_auditUpdate_saveCorrectLog() {
        // given
        Object oldVal = "old", newVal = "new";
        // when
        auditService.auditUpdate("E", 7L, oldVal, newVal);
        // then
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLog log = cap.getValue();
        assertThat(log.getEntityName()).isEqualTo("E");
        assertThat(log.getEntityId()).isEqualTo(7L);
        assertThat(log.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(log.getOldValue()).isEqualTo("{\"foo\":\"bar\"}");
        assertThat(log.getNewValue()).isEqualTo("{\"foo\":\"bar\"}");
        assertCommon(log);
    }

    @Test
    @DisplayName("auditAction should build custom action log with string values")
    void should_auditAction_saveCorrectLog() {
        // when
        auditService.auditAction(AuditAction.ASSET_BLOCKED, "Asset", 5L, "o", "n");
        // then
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLog log = cap.getValue();
        assertThat(log.getEntityName()).isEqualTo("Asset");
        assertThat(log.getEntityId()).isEqualTo(5L);
        assertThat(log.getAction()).isEqualTo(AuditAction.ASSET_BLOCKED);
        assertThat(log.getOldValue()).isEqualTo("o");
        assertThat(log.getNewValue()).isEqualTo("n");
        assertCommon(log);
    }

    @Test
    @DisplayName("auditLogin should record LOGIN and LOGIN_FAILED correctly")
    void should_auditLogin_saveCorrectLog() {
        // when
        auditService.auditLogin(USER_ID, USERNAME, IP, true);
        // then
        ArgumentCaptor<AuditLog> cap1 = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap1.capture());
        AuditLog log1 = cap1.getValue();
        assertThat(log1.getEntityName()).isEqualTo("Customer");
        assertThat(log1.getEntityId()).isEqualTo(USER_ID);
        assertThat(log1.getAction()).isEqualTo(AuditAction.LOGIN);
        assertThat(log1.getNewValue()).isEqualTo("Login successful");
        assertCommon(log1);
        reset(auditLogRepository);
        // when
        auditService.auditLogin(USER_ID, USERNAME, IP, false);
        // then
        ArgumentCaptor<AuditLog> cap2 = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap2.capture());
        AuditLog log2 = cap2.getValue();
        assertThat(log2.getAction()).isEqualTo(AuditAction.LOGIN_FAILED);
        assertThat(log2.getNewValue()).isEqualTo("Login failed");
        assertCommon(log2);
    }

    @Test
    @DisplayName("auditOrderAction should build ORDER action log")
    void should_auditOrderAction_saveCorrectLog() {
        // when
        auditService.auditOrderAction(123L, AuditAction.UPDATE, "details");
        // then
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLog log = cap.getValue();
        assertThat(log.getEntityName()).isEqualTo("Order");
        assertThat(log.getEntityId()).isEqualTo(123L);
        assertThat(log.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(log.getNewValue()).isEqualTo("details");
        assertCommon(log);
    }

    @Test
    @DisplayName("auditBalanceChange should prefix assetName and set old/new balance")
    void should_auditBalanceChange_saveCorrectLog() {
        // when
        auditService.auditBalanceChange(55L, "GOLD", BigDecimal.TEN, BigDecimal.valueOf(8), "reason");
        // then
        ArgumentCaptor<AuditLog> cap = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(cap.capture());
        AuditLog log = cap.getValue();
        assertThat(log.getEntityName()).isEqualTo("Asset-GOLD");
        assertThat(log.getEntityId()).isEqualTo(55L);
        assertThat(log.getAction()).isEqualTo(AuditAction.BALANCE_UPDATED);
        assertThat(log.getOldValue()).isEqualTo("10");
        assertThat(log.getNewValue()).isEqualTo("8");
        assertCommon(log);
    }

    private void assertCommon(AuditLog log) {
        assertThat(log.getUserId()).isEqualTo(USER_ID);
        assertThat(log.getUsername()).isEqualTo(USERNAME);
        assertThat(log.getIpAddress()).isEqualTo(IP);
        assertThat(log.getUserAgent()).isEqualTo(USER_AGENT);
        assertThat(log.getRequestId()).isEqualTo(REQUEST_ID);
        assertThat(log.getActionDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}