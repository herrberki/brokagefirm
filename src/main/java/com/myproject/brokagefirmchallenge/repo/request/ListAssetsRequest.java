package com.myproject.brokagefirmchallenge.repo.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ListAssetsRequest extends BaseRequest {

    private String assetName;

    @DecimalMin(value = "0", message = "Min size must be non-negative")
    private BigDecimal minSize;

    @DecimalMin(value = "0", message = "Min usable size must be non-negative")
    private BigDecimal minUsableSize;

    private boolean includeZeroBalance = false;
}
