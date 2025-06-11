package com.myproject.brokagefirmchallenge.repo.service.impl;

import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import com.myproject.brokagefirmchallenge.repo.enumtype.AuditAction;
import com.myproject.brokagefirmchallenge.repo.exceptions.AssetNotFoundException;
import com.myproject.brokagefirmchallenge.repo.exceptions.InsufficientBalanceException;
import com.myproject.brokagefirmchallenge.repo.repository.AssetRepository;
import com.myproject.brokagefirmchallenge.repo.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceImplTest {

    @Mock private AssetRepository assetRepository;
    @Mock private AuditService auditService;
    @InjectMocks private AssetServiceImpl assetService;

    private static final Long CUST = 1L;
    private static final String ASSET = "BTC";

    private Asset existing;

    @BeforeEach
    void setup() {
        existing = new Asset();
        existing.setId(2L);
        existing.setCustomerId(CUST);
        existing.setAssetName(ASSET);
        existing.setSize(new BigDecimal("10"));
        existing.setUsableSize(new BigDecimal("8"));
    }

    @Test
    @DisplayName("should_createOrUpdateAsset_updateExisting_inOrder")
    void should_createOrUpdateAsset_updateExisting_inOrder() {
        // given
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        when(assetRepository.save(existing)).thenReturn(existing);
        // when
        assetService.createOrUpdateAsset(CUST, ASSET, new BigDecimal("2"));
        // then
        InOrder in = inOrder(assetRepository, auditService);
        in.verify(assetRepository).findByCustomerIdAndAssetNameForUpdate(CUST, ASSET);
        in.verify(assetRepository).save(existing);
        in.verify(auditService).auditBalanceChange(
                eq(CUST), eq(ASSET),
                eq(new BigDecimal("10")), eq(new BigDecimal("12")),
                anyString());
        in.verifyNoMoreInteractions();
        assertThat(existing.getSize()).isEqualByComparingTo("12");
        assertThat(existing.getUsableSize()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("should_createOrUpdateAsset_createNew_inOrder")
    void should_createOrUpdateAsset_createNew_inOrder() {
        // given
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.empty());
        ArgumentCaptor<Asset> cap = ArgumentCaptor.forClass(Asset.class);
        when(assetRepository.save(any())).thenAnswer(a -> {
            Asset aS = a.getArgument(0);
            aS.setId(3L);
            return aS;
        });
        // when
        assetService.createOrUpdateAsset(CUST, ASSET, new BigDecimal("5"));
        // then
        InOrder in = inOrder(assetRepository, auditService);
        in.verify(assetRepository).findByCustomerIdAndAssetNameForUpdate(CUST, ASSET);
        in.verify(assetRepository).save(cap.capture());
        in.verify(auditService).auditCreate("Asset", 3L, cap.getValue());
        in.verifyNoMoreInteractions();
        Asset created = cap.getValue();
        assertThat(created.getCustomerId()).isEqualTo(CUST);
        assertThat(created.getAssetName()).isEqualTo(ASSET);
        assertThat(created.getSize()).isEqualByComparingTo("5");
        assertThat(created.getUsableSize()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("should_findByCustomerIdAndAssetNameOrThrow_throw_whenAbsent")
    void should_findByCustomerIdAndAssetNameOrThrow_throw() {
        // given
        when(assetRepository.findByCustomerIdAndAssetName(CUST, ASSET))
                .thenReturn(Optional.empty());
        // when // then
        assertThatThrownBy(() ->
                assetService.findByCustomerIdAndAssetNameOrThrow(CUST, ASSET))
                .isInstanceOf(AssetNotFoundException.class);
    }

    @Test
    @DisplayName("should_findAssets_delegate")
    void should_findAssets_delegate() {
        // given
        var spec = (Specification<Asset>)(r, q, cb) -> null;
        List<Asset> list = Collections.singletonList(existing);
        when(assetRepository.findAll(spec)).thenReturn(list);
        // when
        var result = assetService.findAssets(spec);
        // then
        assertThat(result).isSameAs(list);
        verify(assetRepository).findAll(spec);
    }

    @Test
    @DisplayName("should_blockAsset_success_inOrder")
    void should_blockAsset_success_inOrder() {
        // given
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        when(assetRepository.save(existing)).thenReturn(existing);
        // when
        assetService.blockAsset(CUST, ASSET, new BigDecimal("3"));
        // then
        InOrder in = inOrder(assetRepository, auditService);
        in.verify(assetRepository).findByCustomerIdAndAssetNameForUpdate(CUST, ASSET);
        in.verify(assetRepository).save(existing);
        in.verify(auditService).auditAction(
                AuditAction.ASSET_BLOCKED, "Asset", 2L,
                "8", "5");
        in.verifyNoMoreInteractions();
        assertThat(existing.getUsableSize()).isEqualByComparingTo("5");
    }

    @Test
    @DisplayName("should_blockAsset_insufficientBalance_throw")
    void should_blockAsset_insufficientBalance_throw() {
        // given
        existing.setUsableSize(new BigDecimal("1"));
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        // when // then
        assertThatThrownBy(() ->
                assetService.blockAsset(CUST, ASSET, new BigDecimal("2")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("should_releaseAsset_success_inOrder")
    void should_releaseAsset_success_inOrder() {
        // given
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        when(assetRepository.save(existing)).thenReturn(existing);
        // when
        assetService.releaseAsset(CUST, ASSET, new BigDecimal("5"));
        // then
        InOrder in = inOrder(assetRepository, auditService);
        in.verify(assetRepository).findByCustomerIdAndAssetNameForUpdate(CUST, ASSET);
        in.verify(assetRepository).save(existing);
        in.verify(auditService).auditAction(
                AuditAction.ASSET_RELEASED, "Asset", 2L,
                "8", "10");
        in.verifyNoMoreInteractions();
        assertThat(existing.getUsableSize()).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("should_transferAsset_success_inOrder")
    void should_transferAsset_success_inOrder() {
        // given
        Asset from = new Asset();
        from.setId(4L);
        from.setUsableSize(new BigDecimal("5"));
        from.setSize(new BigDecimal("5"));
        from.setCustomerId(CUST);
        from.setAssetName(ASSET);
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(from));
        doNothing().when(assetService).createOrUpdateAsset(anyLong(), any(), any());
        when(assetRepository.save(from)).thenReturn(from);
        // when
        assetService.transferAsset(CUST, 9L, ASSET, new BigDecimal("3"));
        // then
        InOrder in = inOrder(assetRepository, assetService, auditService);
        in.verify(assetRepository).findByCustomerIdAndAssetNameForUpdate(CUST, ASSET);
        in.verify(assetRepository).save(from);
        in.verify(assetService).createOrUpdateAsset(9L, ASSET, new BigDecimal("3"));
        in.verify(auditService).auditAction(
                AuditAction.BALANCE_UPDATED, "Asset", 4L,
                "Transfer out: 3", "To customer: 9");
        in.verifyNoMoreInteractions();
        assertThat(from.getSize()).isEqualByComparingTo("2");
    }

    @Test
    @DisplayName("should_transferAsset_insufficientBalance_throw")
    void should_transferAsset_insufficientBalance_throw() {
        // given
        existing.setUsableSize(new BigDecimal("1"));
        when(assetRepository.findByCustomerIdAndAssetNameForUpdate(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        // when // then
        assertThatThrownBy(() ->
                assetService.transferAsset(CUST, 9L, ASSET, new BigDecimal("2")))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    @DisplayName("should_getUsable_and_getTotalBalance")
    void should_getUsable_and_getTotalBalance() {
        // given
        when(assetRepository.findByCustomerIdAndAssetName(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        // when
        BigDecimal u = assetService.getUsableBalance(CUST, ASSET);
        BigDecimal t = assetService.getTotalBalance(CUST, ASSET);
        // then
        assertThat(u).isEqualByComparingTo("8");
        assertThat(t).isEqualByComparingTo("10");
    }

    @Test
    @DisplayName("should_initializeCustomerAssets_save_whenAbsent")
    void should_initializeCustomerAssets_save_whenAbsent() {
        // given
        when(assetRepository.existsByCustomerIdAndAssetName(CUST, "TRY"))
                .thenReturn(false);
        // when
        assetService.initializeCustomerAssets(CUST);
        // then
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    @DisplayName("should_initializeCustomerAssets_notSave_whenPresent")
    void should_initializeCustomerAssets_notSave_whenPresent() {
        // given
        when(assetRepository.existsByCustomerIdAndAssetName(CUST, "TRY"))
                .thenReturn(true);
        // when
        assetService.initializeCustomerAssets(CUST);
        // then
        verify(assetRepository, never()).save(any());
    }

    @Test
    @DisplayName("should_isEnoughBalance")
    void should_isEnoughBalance() {
        // given
        when(assetRepository.findByCustomerIdAndAssetName(CUST, ASSET))
                .thenReturn(Optional.of(existing));
        // when
        boolean ok = assetService.isEnoughBalance(CUST, ASSET, new BigDecimal("5"));
        // then
        assertThat(ok).isTrue();
    }
}
