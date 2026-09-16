package com.jippy.foodandmart.dto;
import lombok.Data;

@Data
public class FmMerchantAdminFilterDto {

    /**
     * Search by merchant name, email or phone
     */
    private String search;

    /**
     * Restaurant, Grocery, Pharmacy, etc.
     */
    private String merchantBusinessType;

    /**
     * Filter merchant by area
     */
    private Integer areaId;

    /**
     * Y = Active
     * N = Inactive
     */
    private String isActive;

    /**
     * true = Approved
     * false = Not Approved
     */
    private Boolean isApproved;

    /**
     * PENDING, APPROVED, REJECTED, etc.
     */
    private String status;

}

