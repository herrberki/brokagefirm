package com.myproject.brokagefirmchallenge.repo.validator;

import com.myproject.brokagefirmchallenge.repo.exceptions.ValidationException;
import com.myproject.brokagefirmchallenge.repo.request.ListAssetsRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AssetValidator extends BaseValidator<ListAssetsRequest> {

    @Override
    public void validate(ListAssetsRequest request) {
        validateMinSize(request.getMinSize());
        validateMinUsableSize(request.getMinUsableSize());
        validateAssetName(request.getAssetName());
    }

    private void validateMinSize(BigDecimal minSize) {
        if (minSize != null && minSize.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Min size cannot be negative");
        }
    }

    private void validateMinUsableSize(BigDecimal minUsableSize) {
        if (minUsableSize != null && minUsableSize.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Min usable size cannot be negative");
        }
    }

    private void validateAssetName(String assetName) {
        if (assetName != null && !assetName.isEmpty()) {
            if (!assetName.matches("^[A-Z]{2,10}$")) {
                throw new ValidationException("Invalid asset name format");
            }
        }
    }

    public void validateTransfer(Long fromCustomerId, Long toCustomerId, String assetName, BigDecimal amount) {
        if (fromCustomerId.equals(toCustomerId)) {
            throw new ValidationException("Cannot transfer to the same customer");
        }

        validateNotNull(assetName, "Asset name");
        validateNotNull(amount, "Transfer amount");
        validatePositive(amount);

        if (!assetName.matches("^[A-Z]{2,10}$")) {
            throw new ValidationException("Invalid asset name format");
        }
    }
}
