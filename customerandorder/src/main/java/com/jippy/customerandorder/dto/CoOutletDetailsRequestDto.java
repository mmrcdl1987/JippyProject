package com.jippy.customerandorder.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO used by CO to request multiple outlet details
 * from the FM microservice.
 */
@Getter
@Setter
public class CoOutletDetailsRequestDto {

    private List<Integer> outletIds;
}