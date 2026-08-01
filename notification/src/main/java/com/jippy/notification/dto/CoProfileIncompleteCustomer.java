package com.jippy.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoProfileIncompleteCustomer {

    private Integer customerId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;

}