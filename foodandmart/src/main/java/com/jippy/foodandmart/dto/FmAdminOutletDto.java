package com.jippy.foodandmart.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FmAdminOutletDto {

    private Integer outletId;

    private String outletName;

    private String outletType;

    private String outletEmail;

    private String outletPhone;

    private String alternateOutletPhone;

    private String outletPicUrl;

    private Integer merchantId;

    private String merchantName;


    private Integer areaId;

    private String areaName;

    private BigDecimal totalRating;

    private Integer totalReviews;

    private String isActive;

    private Boolean isApproved;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
