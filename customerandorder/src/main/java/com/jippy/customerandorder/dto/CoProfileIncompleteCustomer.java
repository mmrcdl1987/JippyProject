package com.jippy.customerandorder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoProfileIncompleteCustomer {

    private Integer customerId;

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String email;



}