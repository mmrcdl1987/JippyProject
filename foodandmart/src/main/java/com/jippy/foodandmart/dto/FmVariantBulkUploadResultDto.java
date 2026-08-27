package com.jippy.foodandmart.dto;

import com.jippy.foodandmart.enums.FmVariantBulkUploadStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FmVariantBulkUploadResultDto {

    private Integer rowNumber;

    private String productName;

    private String variantGroupName;

    private String variantGroupValue;

    private String priceType;

    private BigDecimal variantPrice;

    private FmVariantBulkUploadStatus status;

    private String message;

    private Integer productId;

    private Integer variantGroupId;

    private Integer variantGroupValueId;
}