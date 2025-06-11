package com.myproject.brokagefirmchallenge.repo.repository;

import com.myproject.brokagefirmchallenge.repo.entity.Asset;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AssetRepository extends BaseRepository<Asset, Long> {

    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.customerId = :customerId AND a.assetName = :assetName")
    Optional<Asset> findByCustomerIdAndAssetNameForUpdate(@Param("customerId") Long customerId,
                                                          @Param("assetName") String assetName);

    boolean existsByCustomerIdAndAssetName(Long customerId, String assetName);
}
