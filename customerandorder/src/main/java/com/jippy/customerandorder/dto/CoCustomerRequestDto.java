

package com.jippy.customerandorder.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class CoCustomerRequestDto {

    private String firstName;

    private String lastName;

    private String email;

    private String phoneNumber;

    private LocalDate DOB;

    private Integer createdBy;

    private String referralCode;

    private String referralCodeUsed;

    private Integer customerId;

    private String fcmToken;

}