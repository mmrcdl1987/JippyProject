

package com.jippy.customerandorder.dto;

import lombok.Data;

@Data
public class CoCustomerRequestDto {

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private Integer createdBy;


    private String referralCode;

    private Integer customerId;

    private String profilePicUrl;
}