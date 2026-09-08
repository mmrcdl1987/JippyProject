package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used by CO to request multiple driver details
 * from the Driver microservice.
 */
@Getter
@Setter
public class CoDriverDetailsRequestDto {

    private List<Integer> driverIds;
}